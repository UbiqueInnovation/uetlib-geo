package ch.ubique.backend.geo.tile.vector.utils;

import static java.lang.Math.min;

import ch.ubique.backend.core.utils.ProgressLogger;
import ch.ubique.backend.geo.tile.common.encoder.TileEncoderFactory;
import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.utils.TileGenerator;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;
import org.geotools.geometry.jts.JTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VectorTileGenerator implements TileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileGenerator.class);

    private final VectorTileOutput output;

    private final ForkJoinPool forkJoinPool;

    public VectorTileGenerator(final VectorTileOutput output, final int parallelism) {
        this.output = output;
        this.forkJoinPool = new ForkJoinPool(parallelism);
    }

    public static void generateTiles(final VectorTileOutput output)
            throws ExecutionException, InterruptedException {
        generateTiles(output, 64);
    }

    public static void generateTiles(final VectorTileOutput output, int parallelism)
            throws ExecutionException, InterruptedException {
        final var generator = new VectorTileGenerator(output, parallelism);
        generator.generateTiles();
        generator.forkJoinPool.shutdown();
    }

    @Override
    public void generateTiles() throws ExecutionException, InterruptedException {
        for (final var level : output.config().getZoomLevels()) {
            generateTilesForLevel(level);
        }
    }

    private void generateTilesForLevel(final int level)
            throws ExecutionException, InterruptedException {
        final var tiles = getTiles(level);

        final var tileSizes = forkJoinPool.submit(() -> generateTileCollection(tiles)).get();

        printStats(tiles, tileSizes, level);
    }

    @NotNull
    private List<Integer> generateTileCollection(final Collection<VectorTile> tiles) {
        final var progressLogger = new ProgressLogger(tiles.size());
        return tiles.parallelStream().map(progressLogger.watch(this::generateTile)).toList();
    }

    private int generateTile(final Tile tile) {
        final var encoder = TileEncoderFactory.getEncoder(output.config());
        final var binaryTile = encoder.encode(tile);
        consumeTile(binaryTile);
        return binaryTile.bytes().length;
    }

    private void consumeTile(final BinaryTile binaryTile) {
        Optional.ofNullable(output.tileConsumer())
                .ifPresent(consumer -> consumer.uncheck().accept(binaryTile));
    }

    @NotNull
    private Collection<VectorTile> getTiles(final int level) {
        logger.info("Getting tiles for zoom level {}", level);

        return output.config()
                .getTileLevelsDefinition()
                .tileInfos(level)
                .map(this::createTile)
                .toList();
    }

    @NotNull
    private VectorTile createTile(final TileInfo tileInfo) {
        final var elementsOnTile = elementsOnTile(tileInfo).toList();
        return new VectorTile(tileInfo, elementsOnTile);
    }

    private void printStats(
            final Collection<VectorTile> tiles, final List<Integer> tileSizes, final int level) {

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
    private Stream<VectorTileElement> elementsOnTile(final TileInfo tileInfo) {
        final var envelopeGeometry = JTS.toGeometry(tileInfo.envelope());

        final var elementsOnTile =
                output.elements(tileInfo).stream()
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
