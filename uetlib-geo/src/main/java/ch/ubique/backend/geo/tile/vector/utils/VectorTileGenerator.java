package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.core.utils.MathUtils;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static ch.ubique.backend.geo.coordinate.util.PointConverter.toPoint;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class VectorTileGenerator<T extends VectorTileElement> {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileGenerator.class);

    private final TileLevelsDefinition tileLevelsDefinition;

    private final VectorTileOutput<T> output;

    private final ForkJoinPool forkJoinPool;

    public static <S extends VectorTileElement> void generateForSwitzerlandLv95(VectorTileOutput<S> output) throws ExecutionException, InterruptedException {
        final var generator = new VectorTileGenerator<>(SwissConstants.swissTileDefinition(), output, 4);
        generator.generateTiles();
        generator.forkJoinPool.shutdown();
    }

    private VectorTileGenerator(TileLevelsDefinition tileLevelsDefinition, VectorTileOutput<T> output, int numThreads) {
        this.tileLevelsDefinition = tileLevelsDefinition;
        this.output = output;
        this.forkJoinPool = new ForkJoinPool(numThreads);
    }

    public void generateTiles() throws ExecutionException, InterruptedException {
        for (var level : output.config().getZoomLevels()) {
            generateTilesForLevel(level);
        }
    }

    private void generateTilesForLevel(int level) throws ExecutionException, InterruptedException {
        final var tiles = getTiles(level);
        final var count = new AtomicInteger();
        final int onePercent = max(1, tiles.size() / 100);
        final long startTime = System.currentTimeMillis();

        var tileSizes = forkJoinPool.submit(() ->
                tiles.parallelStream()
                        .map(this::generateTile)
                        .peek(i -> logProgress(tiles, count, onePercent, startTime))
                        .toList()
        ).get();

        printStats(tiles, tileSizes, level);
    }

    private int generateTile(Tile<T> tile) {
        var binaryTile = VectorTileEncoderUtil.encode(tile, output.config());
        output.tileConsumer().uncheck().accept(binaryTile);
        return binaryTile.bytes().length;
    }

    private void logProgress(Collection<Tile<T>> tiles, AtomicInteger count, int onePercent, long startTime) {
        int countValue = count.incrementAndGet();
        if (countValue % onePercent == 0) {
            int percent = 100 * countValue / tiles.size();
            long currentTime = System.currentTimeMillis();
            double tilesPerSecond = countValue / ((double) (currentTime - startTime)) * 1000.0;
            logger.info("Processed {}% ({} tiles per second)", percent, tilesPerSecond);
        }
    }

    private void printStats(Collection<Tile<T>> tiles, List<Integer> tileSizes, int level) {

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

        if (output.config().limitElementsPerTile(level)) {
            int maxElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    output.config().getMaxElementsPerTile(level),
                                                    tile.elements().size()))
                            .max()
                            .orElse(-1);
            double avgElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    output.config().getMaxElementsPerTile(level),
                                                    tile.elements().size()))
                            .average()
                            .orElse(-1);
            stats.append(
                    String.format(
                            "  Elements per tile (with limit): avg = %.2f, max = %d%n",
                            avgElementsWithLimit, maxElementsWithLimit));
        }

        int maxElements = tiles.stream().mapToInt(tile -> tile.elements().size()).max().orElse(-1);
        double avgElements =
                tiles.stream().mapToInt(tile -> tile.elements().size()).average().orElse(-1);
        stats.append(
                String.format(
                        "  Elements per tile (without limit): avg = %.2f, max = %d",
                        avgElements, maxElements));

        logger.info("Statistic for level {}: {}", level, stats);
    }

    private Collection<Tile<T>> getTiles(int level) {
        final var zoomLevel = tileLevelsDefinition.getZoomLevel(level);
        final var tilesX = zoomLevel.tilesX();
        final var tilesY = zoomLevel.tilesY();
        final var tileWidth = zoomLevel.tileWidth();
        final var origin = toPoint(Point2d.class, tileLevelsDefinition.origin());

        logger.info("Generate tiles for zoom level {}", level);

        List<Tile<T>> tiles = new ArrayList<>(tilesX * tilesY);

        for (int x = 0; x < tilesX; ++x) {
            for (int y = 0; y < tilesY; ++y) {
                Point2d min = origin.plus(tileWidth * x, -tileWidth * (y + 1));
                Point2d max = min.plus(tileWidth, tileWidth);
                TileInfo tileInfo = new TileInfo(x, y, zoomLevel);
                final var boundingBox =
                        new Envelope(min.getX(), max.getX(), min.getY(), max.getY());
                tiles.add(new Tile<>(tileInfo, boundingBox));
            }
        }

        // Given a coordinate, return corresponding tile coordinates
        ToIntFunction<Double> getX =
                xCoordinate -> (int) ((xCoordinate - origin.getX()) / tileWidth);
        ToIntFunction<Double> getY =
                yCoordinate -> (int) ((origin.getY() - yCoordinate) / tileWidth);

        // Add elements to tiles that lie within bounding box of element and are visible at the
        // current zoom level
        for (T element : output.elements()) {
            if (element.geometry().getSRID() != tileLevelsDefinition.srid()) {
                throw new MismatchedReferenceSystemException(
                        String.format(
                                "The elements (%s) srid (%d) does not match the grids srid (%d)",
                                element, element.geometry().getSRID(), tileLevelsDefinition.srid()));
            }

            if (element.showElement(zoomLevel.level())) {
                getXYsElementIsIn(tilesX, tilesY, getX, getY, element)
                        .forEach(
                                xy ->
                                        tiles.get(xy.getLeft() * tilesY + xy.getRight())
                                                .elements()
                                                .add(element));
            }
        }
        logger.info("Number of tiles for zoom level {}: {}", level, tiles.size());
        return limitElementsPerTile(tiles, zoomLevel);
    }

    public List<Pair<Integer, Integer>> getXYsElementIsIn(T element, int z) {
        final ZoomLevel zoomLevel = tileLevelsDefinition.getZoomLevel(z);
        final double tileWidth = zoomLevel.tileWidth();
        final Point2d origin = toPoint(Point2d.class, tileLevelsDefinition.origin());
        ToIntFunction<Double> getX =
                coordinateX -> (int) ((coordinateX - origin.getX()) / tileWidth);
        ToIntFunction<Double> getY =
                coordinateY -> (int) ((origin.getY() - coordinateY) / tileWidth);
        return getXYsElementIsIn(zoomLevel.tilesX(), zoomLevel.tilesY(), getX, getY, element);
    }

    private List<Pair<Integer, Integer>> getXYsElementIsIn(
            int tilesX,
            int tilesY,
            ToIntFunction<Double> getX,
            ToIntFunction<Double> getY,
            T element) {
        final List<Pair<Integer, Integer>> xyList = new ArrayList<>();

        final var box = element.boundingBox();

        final var x1 = MathUtils.clamp(getX.applyAsInt(box.getMinX()), 0, tilesX - 1);
        final var x2 = MathUtils.clamp(getX.applyAsInt(box.getMaxX()), 0, tilesX - 1);
        final var y1 = MathUtils.clamp(getY.applyAsInt(box.getMinY()), 0, tilesY - 1);
        final var y2 = MathUtils.clamp(getY.applyAsInt(box.getMaxY()), 0, tilesY - 1);

        final var minX = min(x1, x2);
        final var maxX = Math.max(x1, x2);
        final var minY = min(y1, y2);
        final var maxY = Math.max(y1, y2);

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

        if (!output.config().limitElementsPerTile(zoomLevel.level())) {
            return tiles;
        }

        var maxElementsPerTile = output.config().getMaxElementsPerTile(zoomLevel.level());
        logger.info("Limiting number of elements per tile to: {}", maxElementsPerTile);

        // Mark all elements that need to be visible
        var visibleElements = new HashSet<Long>();
        for (var tile : tiles) {
            // only the most important elements are visible
            ToDoubleFunction<VectorTileElement> getScore = VectorTileElement::score;
            var visibleElementsForTile =
                    tile.elements().stream()
                            .filter(output.config()::filter)
                            .sorted(Comparator.comparingDouble(getScore).reversed())
                            .limit(output.config().getMaxElementsPerTile(zoomLevel.level()))
                            .map(VectorTileElement::id)
                            .collect(Collectors.toSet());

            visibleElements.addAll(visibleElementsForTile);
        }

        // Generate tiles that contain only visible elements
        final var limitedTiles = new ArrayList<Tile<T>>(tiles.size());
        for (var tile : tiles) {
            Collection<T> visibleElementsForTile =
                    tile.elements().stream().filter(t -> visibleElements.contains(t.id())).toList();

            var limitedTile = new Tile<T>(tile.tileInfo(), tile.boundingBox());
            limitedTile.elements().addAll(visibleElementsForTile);
            limitedTiles.add(limitedTile);
        }

        logger.info("Filtered elements per tile for zoom level {}", zoomLevel.level());
        return limitedTiles;
    }
}
