package ch.ubique.backend.geo.tile.vector.utils;

import static ch.ubique.backend.geo.coordinate.util.PointConverter.toPoint;
import static java.lang.Math.max;
import static java.lang.Math.min;

import ch.ubique.backend.core.utils.MathUtils;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import no.ecc.vectortile.VectorTileEncoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VectorTileHelper<T extends VectorTileElement> {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileHelper.class);

    private final TileLevelsDefinition gridInfo;

    private final VectorTileConfig<T> config;
    private final Collection<T> elements;

    public VectorTileHelper(Collection<T> elements, VectorTileConfig<T> config) {
        this.config = config;
        this.elements = elements;
        this.gridInfo = SwissConstants.swissTileDefinition();
    }

    private byte[] generateBinary(Tile<T> tile) throws FactoryException {
        final var encoder =
                new VectorTileEncoder(config.getExtent(), config.getClipBuffer(), false, false);

        for (final var element : tile.elements()) {
            addFeature(encoder, tile, element);
        }
        return encoder.encode();
    }

    private void addFeature(
            final VectorTileEncoder encoder, final Tile<T> tile, final VectorTileElement element)
            throws FactoryException {
        if (!element.showElement(tile.tileInfo().zoomLevel().level())) {
            return;
        }
        final var boundingBox = tile.boundingBox();
        final var layer = element.layer();
        final long id = element.id();
        final var tags = VectorTileElementUtils.getFinalTags(element);

        final Geometry geometry;
        try {
            geometry = JtsUtil.tryToGetValidGeometry(element.geometry());
        } catch (UnfixableGeometryException e) {
            logger.warn("Element geometry for {} not valid and could not be fixed", tags, e);
            return;
        }

        final var tileGeometryMaybe =
                TileGeometryUtils.getTileGeometry(
                        geometry, boundingBox, config.getExtent(), config.getYAxisDirection());
        if (tileGeometryMaybe.isEmpty()) {
            logger.warn("Could not get tile geometry for {}", tags);
            return;
        }

        final Geometry tileGeometry;
        try {
            tileGeometry = JtsUtil.tryToGetValidGeometry(tileGeometryMaybe.get());
        } catch (UnfixableGeometryException e) {
            logger.warn("Tile geometry for {} not valid and could not be fixed", tags, e);
            return;
        }

        final var outputGeometry =
                config.simplify()
                        ? SimplifyGeometryUtils.simplify(tileGeometry, config.simplifyTolerance())
                        : tileGeometry;

        encoder.addFeature(layer, tags, outputGeometry, id);
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
                                                            ThrowingFunction<
                                                                            Tile<T>,
                                                                            byte[],
                                                                            Exception>
                                                                    generateFunction =
                                                                            this::generateBinary;
                                                            byte[] bytes =
                                                                    generateFunction
                                                                            .uncheck()
                                                                            .apply(tile);

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
                                                                            tile.tileInfo(), bytes);
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

        if (config.limitElementsPerTile(zoomLevel.level())) {
            int maxElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    config.getMaxElementsPerTile(zoomLevel.level()),
                                                    tile.elements().size()))
                            .max()
                            .orElse(-1);
            double avgElementsWithLimit =
                    tiles.stream()
                            .mapToInt(
                                    tile ->
                                            min(
                                                    config.getMaxElementsPerTile(zoomLevel.level()),
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

        logger.info("Statistic for level {}: {}", zoomLevel.level(), stats);
    }

    private Collection<Tile<T>> getTiles(ZoomLevel zoomLevel) {
        int tilesX = zoomLevel.tilesX();
        int tilesY = zoomLevel.tilesY();
        int level = zoomLevel.level();
        double tileWidth = zoomLevel.tileWidth();
        Point2d origin = toPoint(Point2d.class, gridInfo.origin());

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
        for (T element : elements) {
            if (element.geometry().getSRID() != gridInfo.srid()) {
                throw new MismatchedReferenceSystemException(
                        String.format(
                                "The elements (%s) srid (%d) does not match the grids srid (%d)",
                                element, element.geometry().getSRID(), gridInfo.srid()));
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
        final ZoomLevel zoomLevel = gridInfo.getZoomLevel(z);
        final double tileWidth = zoomLevel.tileWidth();
        final Point2d origin = toPoint(Point2d.class, gridInfo.origin());
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

        final var minX = Math.min(x1, x2);
        final var maxX = Math.max(x1, x2);
        final var minY = Math.min(y1, y2);
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

        if (!config.limitElementsPerTile(zoomLevel.level())) {
            return tiles;
        }

        var maxElementsPerTile = config.getMaxElementsPerTile(zoomLevel.level());
        logger.info("Limiting number of elements per tile to: {}", maxElementsPerTile);

        // Mark all elements that need to be visible
        var visibleElements = new HashSet<Long>();
        for (var tile : tiles) {
            // only the most important elements are visible
            ToDoubleFunction<VectorTileElement> getScore = VectorTileElement::score;
            var visibleElementsForTile =
                    tile.elements().stream()
                            .filter(config::filter)
                            .sorted(Comparator.comparingDouble(getScore).reversed())
                            .limit(config.getMaxElementsPerTile(zoomLevel.level()))
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
