package ch.ubique.backend.geo.vectortile.utils;

import static ch.ubique.backend.geo.coordinate.util.PointConverter.toPoint;
import static ch.ubique.backend.geo.vectortile.constants.SwissConstants.swissGridInfo;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ch.ubique.backend.geo.geometry.models.BoundingBox;
import ch.ubique.backend.geo.geometry.models.BoundingBox2d;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.vectortile.models.*;

import com.pivovarit.function.ThrowingConsumer;

import no.ecc.vectortile.VectorTileEncoder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class VectorTileHelper<T extends TileElement> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GridInfo gridInfo;

    private final VectorTileConfig<T> config;
    private final Collection<T> elements;

    public VectorTileHelper(Collection<T> elements, VectorTileConfig<T> config) {
        this.config = config;
        this.elements = elements;
        this.gridInfo = swissGridInfo();
    }

    private byte[] generateBinary(Tile<T> tile) {
        final int extend = 4096;
        final var encoder = new VectorTileEncoder(extend, 0, false, false);

        for (TileElement element : tile.getElements()) {
            if (!element.showElement(tile.getTileInfo().zoomLevel())) {
                continue;
            }
            final var boundingBox = tile.getBoundingBox();
            final var layer = element.getLayer();
            final long id = element.getId();
            final var item = element.getGeometry(boundingBox, extend, config.getYAxisDirection());
            if (item == null || item.getGeometry() == null) {
                logger.warn("Item or geometry is null: {}", element);
            } else if (!item.getGeometry().isValid()) {
                logger.warn("Geometry not valid: {}", item.getAttributes());
            } else {
                encoder.addFeature(layer, item.getAttributes(), item.getGeometry(), id);
            }
        }
        return encoder.encode();
    }

    public void generateTiles(
            ThrowingConsumer<BinaryTile, IOException> tileConsumer, int numThreads)
            throws ExecutionException, InterruptedException {
        final var levels =
                config.getZoomLevels().stream()
                        .sorted(Comparator.comparingInt((Integer i) -> i).reversed())
                        .toList();

        for (var level : levels) {
            final var zoomLevel = gridInfo.getZoomLevel(level);
            if (config.simplify()) {
                simplifyElements(zoomLevel);
            }
            generateTilesForLevel(zoomLevel, tileConsumer, numThreads);
        }
    }

    public Collection<BinaryTile> generateTiles() throws ExecutionException, InterruptedException {
        final Collection<BinaryTile> binaryTiles = new ConcurrentLinkedQueue<>();
        generateTiles(binaryTiles::add, 4);
        return binaryTiles;
    }

    public Collection<BinaryTile> generateTiles(int numThreads)
            throws ExecutionException, InterruptedException {
        final Collection<BinaryTile> binaryTiles = new ConcurrentLinkedQueue<>();
        generateTiles(binaryTiles::add, numThreads);
        return binaryTiles;
    }

    private void simplifyElements(ZoomLevel zoomLevel) {
        logger.info("Simplifying elements");
        double sum = elements.stream().mapToDouble(element -> element.simplify(zoomLevel)).sum();
        logger.info("Relative size after simplifying (avg): {}", sum / elements.size());
    }

    private void generateTilesForLevel(
            ZoomLevel zoomLevel,
            ThrowingConsumer<BinaryTile, IOException> tileConsumer,
            int numThreads)
            throws ExecutionException, InterruptedException {
        final var tiles = getTiles(zoomLevel);
        final var count = new AtomicInteger();
        final int onePercent = max(1, tiles.size() / 100);
        final long startTime = System.currentTimeMillis();

        final var forkJoinPool = new ForkJoinPool(numThreads);

        var tileSizes =
                forkJoinPool
                        .submit(
                                () ->
                                        tiles.parallelStream()
                                                .map(
                                                        tile -> {
                                                            byte[] bytes = generateBinary(tile);

                                                            // Log progress
                                                            int countValue =
                                                                    count.incrementAndGet();
                                                            if (countValue % onePercent == 0) {
                                                                int percent =
                                                                        100
                                                                                * countValue
                                                                                / tiles.size();
                                                                long currentTime =
                                                                        System.currentTimeMillis();
                                                                double tilesPerSecond =
                                                                        countValue
                                                                                / ((double)
                                                                                        (currentTime
                                                                                                - startTime))
                                                                                * 1000.0;
                                                                logger.info(
                                                                        "Processed {}% ({} tiles"
                                                                            + " per second)",
                                                                        percent, tilesPerSecond);
                                                            }

                                                            var binaryTile =
                                                                    new BinaryTile(
                                                                            tile.getTileInfo(),
                                                                            bytes);
                                                            if (tileConsumer != null) {
                                                                tileConsumer
                                                                        .uncheck()
                                                                        .accept(binaryTile);
                                                            }

                                                            return bytes.length;
                                                        })
                                                .toList())
                        .get();

        printStats(tiles, tileSizes, zoomLevel);

        forkJoinPool.shutdown();
    }

    private void printStats(
            Collection<Tile<T>> tiles, List<Integer> tileSizes, ZoomLevel zoomLevel) {

        StringBuilder stats = new StringBuilder();
        stats.append('\n');

        double nonEmptyPercent =
                100.
                        * tileSizes.stream().mapToInt(size -> size > 0 ? 1 : 0).sum()
                        / tileSizes.size();
        stats.append(String.format("  Non empty tiles: %.2f%%%n", nonEmptyPercent));

        int maxBytes = tileSizes.stream().mapToInt(size -> size).max().orElse(-1);
        double avgBytes = tileSizes.stream().mapToInt(size -> size).average().orElse(-1);
        stats.append(String.format("  Tile size: avg = %,.1fB, max = %,dB%n", avgBytes, maxBytes));

        if (config.limitElementsPerTile(zoomLevel.getLevel())) {
            int maxElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    config.getMaxElementsPerTile(
                                                            zoomLevel.getLevel()),
                                                    tile.getElements().size()))
                            .max()
                            .orElse(-1);
            double avgElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    config.getMaxElementsPerTile(
                                                            zoomLevel.getLevel()),
                                                    tile.getElements().size()))
                            .average()
                            .orElse(-1);
            stats.append(
                    String.format(
                            "  Elements per tile (with limit): avg = %.2f, max = %d%n",
                            avgElementsWithLimit, maxElementsWithLimit));
        }

        int maxElements =
                tiles.stream().mapToInt(tile -> tile.getElements().size()).max().orElse(-1);
        double avgElements =
                tiles.stream().mapToInt(tile -> tile.getElements().size()).average().orElse(-1);
        stats.append(
                String.format(
                        "  Elements per tile (without limit): avg = %.2f, max = %d",
                        avgElements, maxElements));

        logger.info("Statistic for level {}: {}", zoomLevel.getLevel(), stats);
    }

    private Collection<Tile<T>> getTiles(ZoomLevel zoomLevel) {
        int tilesX = zoomLevel.getTilesX();
        int tilesY = zoomLevel.getTilesY();
        int level = zoomLevel.getLevel();
        double tileWidth = gridInfo.getTileWidth(zoomLevel);
        Point2d origin = toPoint(Point2d.class, gridInfo.getOrigin());

        logger.info("Generate tiles for zoom level {}", level);

        List<Tile<T>> tiles = new ArrayList<>(tilesX * tilesY);

        for (int x = 0; x < tilesX; ++x) {
            for (int y = 0; y < tilesY; ++y) {
                Point2d min = origin.plus(tileWidth * x, -tileWidth * (y + 1));
                Point2d max = min.plus(tileWidth, tileWidth);
                TileInfo tileInfo = new TileInfo(x, y, level);
                BoundingBox2d boundingBox = new BoundingBox2d(min, max);
                tiles.add(new Tile<>(tileInfo, boundingBox));
            }
        }

        // Given a coordinate, return corresponding tile coordinates
        ToIntFunction<Point2d> getX =
                coordinate -> (int) ((coordinate.getX() - origin.getX()) / tileWidth);
        ToIntFunction<Point2d> getY =
                coordinate -> (int) ((origin.getY() - coordinate.getY()) / tileWidth);

        // Add elements to tiles that lie within bounding box of element and are visible at the
        // current zoom level
        for (T element : elements) {
            if (element.isVisibleAtZoomLevel(zoomLevel)) {
                getXYsElementIsIn(tilesX, tilesY, getX, getY, element)
                        .forEach(
                                xy ->
                                        tiles.get(xy.getLeft() * tilesY + xy.getRight())
                                                .getElements()
                                                .add(element));
            }
        }
        logger.info("Number of tiles for zoom level {}: {}", level, tiles.size());
        return limitElementsPerTile(tiles, zoomLevel);
    }

    public List<Pair<Integer, Integer>> getXYsElementIsIn(T element, int z) {
        final ZoomLevel zoomLevel = gridInfo.getZoomLevel(z);
        final double tileWidth = gridInfo.getTileWidth(zoomLevel);
        final Point2d origin = toPoint(Point2d.class, gridInfo.getOrigin());
        ToIntFunction<Point2d> getX =
                coordinate -> (int) ((coordinate.getX() - origin.getX()) / tileWidth);
        ToIntFunction<Point2d> getY =
                coordinate -> (int) ((origin.getY() - coordinate.getY()) / tileWidth);
        return getXYsElementIsIn(zoomLevel.getTilesX(), zoomLevel.getTilesY(), getX, getY, element);
    }

    private List<Pair<Integer, Integer>> getXYsElementIsIn(
            int tilesX,
            int tilesY,
            ToIntFunction<Point2d> getX,
            ToIntFunction<Point2d> getY,
            T element) {
        final List<Pair<Integer, Integer>> xyList = new ArrayList<>();

        BoundingBox<Point2d> box = element.getBoundingBox();
        int minX = max(0, getX.applyAsInt(box.getMin()));
        int minY = max(0, getY.applyAsInt(box.getMax()));
        int maxX = min(tilesX - 1, getX.applyAsInt(box.getMax()));
        int maxY = min(tilesY - 1, getY.applyAsInt(box.getMin()));

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                //noinspection SuspiciousNameCombination
                xyList.add(new ImmutablePair<>(x, y));
            }
        }
        return xyList;
    }

    private Collection<Tile<T>> limitElementsPerTile(
            Collection<Tile<T>> tiles, ZoomLevel zoomLevel) {

        if (!config.limitElementsPerTile(zoomLevel.getLevel())) {
            return tiles;
        }

        var maxElementsPerTile = config.getMaxElementsPerTile(zoomLevel.getLevel());
        logger.info("Limiting number of elements per tile to: {}", maxElementsPerTile);

        // Mark all elements that need to be visible
        var visibleElements = new HashSet<Long>();
        for (var tile : tiles) {
            // only the most important elements are visible
            var visibleElementsForTile =
                    tile.getElements().stream()
                            .sorted(Comparator.comparingDouble(TileElement::getScore).reversed())
                            .filter(config::filter)
                            .limit(config.getMaxElementsPerTile(zoomLevel.getLevel()))
                            .map(TileElement::getId)
                            .collect(Collectors.toSet());

            visibleElements.addAll(visibleElementsForTile);
        }

        // Generate tiles that contain only visible elements
        final var limitedTiles = new ArrayList<Tile<T>>(tiles.size());
        for (var tile : tiles) {
            Collection<T> visibleElementsForTile =
                    tile.getElements().stream()
                            .filter(t -> visibleElements.contains(t.getId()))
                            .toList();

            var limitedTile = new Tile<T>(tile.getTileInfo(), tile.getBoundingBox());
            limitedTile.getElements().addAll(visibleElementsForTile);
            limitedTiles.add(limitedTile);
        }

        logger.info("Filtered elements per tile for zoom level {}", zoomLevel.getLevel());
        return limitedTiles;
    }
}
