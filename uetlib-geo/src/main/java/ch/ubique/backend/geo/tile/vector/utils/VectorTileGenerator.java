package ch.ubique.backend.geo.tile.vector.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VectorTileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileGenerator.class);

    private final VectorTileOutput output;

    private final ForkJoinPool forkJoinPool;

    private VectorTileGenerator(final VectorTileOutput output, final int numThreads) {
        this.output = output;
        this.forkJoinPool = new ForkJoinPool(numThreads);
    }

    public static void generateTiles(final VectorTileOutput output)
            throws ExecutionException, InterruptedException {
        final var generator = new VectorTileGenerator(output, 256);
        generator.generateTiles();
        generator.forkJoinPool.shutdown();
    }

    public void generateTiles() throws ExecutionException, InterruptedException {
        for (final var level : output.config().getZoomLevels()) {
            generateTilesForLevel(level);
        }
    }

    private void generateTilesForLevel(final int level)
            throws ExecutionException, InterruptedException {
        final var tiles = getTiles(level);
        final var count = new AtomicInteger();
        final int onePercent = max(1, tiles.size() / 100);
        final long startTime = System.currentTimeMillis();

        final var tileSizes =
                forkJoinPool
                        .submit(
                                () ->
                                        tiles.parallelStream()
                                                .map(this::generateTile)
                                                .peek(
                                                        i ->
                                                                logProgress(
                                                                        tiles,
                                                                        count,
                                                                        onePercent,
                                                                        startTime))
                                                .toList())
                        .get();

        printStats(tiles, tileSizes, level);
    }

    @NotNull
    private Collection<Tile> getTiles(final int level) {
        logger.info("Getting tiles for zoom level {}", level);

        return output.config()
                .getTileLevelsDefinition()
                .getZoomLevel(level)
                .tileInfos()
                .map(this::createTile)
                .toList();
    }

    private int generateTile(final Tile tile) {
        final var binaryTile = VectorTileEncoderUtil.encode(tile, output.config());
        output.tileConsumer().uncheck().accept(binaryTile);
        return binaryTile.bytes().length;
    }

    private void logProgress(
            final Collection<Tile> tiles,
            final AtomicInteger count,
            final int onePercent,
            final long startTime) {
        final int countValue = count.incrementAndGet();
        if (0 == countValue % onePercent) {
            final int percent = 100 * countValue / tiles.size();
            final long currentTime = System.currentTimeMillis();
            final double tilesPerSecond =
                    countValue / ((double) (currentTime - startTime)) * 1000.0;
            logger.info("Processed {}% ({} tiles per second)", percent, tilesPerSecond);
        }
    }

    private void printStats(
            final Collection<Tile> tiles, final List<Integer> tileSizes, final int level) {

        final StringBuilder stats = new StringBuilder();
        stats.append('\n');

        final double nonEmptyPercent =
                100.
                        * tileSizes.stream().mapToInt(size -> 0 < size ? 1 : 0).sum()
                        / tileSizes.size();
        stats.append(String.format("  Non empty tiles: %.2f%%%n", nonEmptyPercent));

        final int maxBytes = tileSizes.stream().mapToInt(size -> size).max().orElse(-1);
        final double avgBytes = tileSizes.stream().mapToInt(size -> size).average().orElse(-1);
        stats.append(String.format("  Tile size: avg = %,.1fB, max = %,dB%n", avgBytes, maxBytes));

        final var limit = output.config().getLimit(level);
        if (limit.isPresent()) {
            final int maxElementsWithLimit =
                    tiles.stream()
                            .mapToInt(tile -> min(limit.get(), tile.elements().size()))
                            .max()
                            .orElse(-1);
            final double avgElementsWithLimit =
                    tiles.stream()
                            .mapToInt(tile -> min(limit.get(), tile.elements().size()))
                            .average()
                            .orElse(-1);
            stats.append(
                    String.format(
                            "  Elements per tile (with limit): avg = %.2f, max = %d%n",
                            avgElementsWithLimit, maxElementsWithLimit));
        }

        final int maxElements =
                tiles.stream().mapToInt(tile -> tile.elements().size()).max().orElse(-1);
        final double avgElements =
                tiles.stream().mapToInt(tile -> tile.elements().size()).average().orElse(-1);
        stats.append(
                String.format(
                        "  Elements per tile (without limit): avg = %.2f, max = %d",
                        avgElements, maxElements));

        logger.info("Statistic for level {}: {}", level, stats);
    }

    @NotNull
    private Tile createTile(final TileInfo tileInfo) {
        final var tileEnvelope = tileInfo.envelope(output.config().getTileLevelsDefinition());
        final var elementsOnTile = elementsOnTile(tileInfo, tileEnvelope).toList();
        return new Tile(tileInfo, tileEnvelope, elementsOnTile);
    }

    @NotNull
    private Stream<VectorTileElement> elementsOnTile(
            final TileInfo tileInfo, final ReferencedEnvelope tileEnvelope) {
        final var envelopeGeometry = JTS.toGeometry(tileEnvelope);

        final var elementsOnTile =
                output.elements().stream()
                        .filter(
                                element ->
                                        element.showElement(tileInfo.level())
                                                && element.geometry().intersects(envelopeGeometry));

        final var limit = output.config().getLimit(tileInfo.level());
        if (limit.isEmpty()) {
            return elementsOnTile;
        }

        return elementsOnTile
                .sorted(Comparator.comparingDouble(VectorTileElement::score).reversed())
                .limit(limit.get());
    }
}
