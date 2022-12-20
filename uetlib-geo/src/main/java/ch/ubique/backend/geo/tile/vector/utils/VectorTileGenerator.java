package ch.ubique.backend.geo.tile.vector.utils;

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
import java.util.Optional;
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

    public static void generateTiles(final VectorTileOutput output) {
        generateTiles(output, 64);
    }

    public static void generateTiles(final VectorTileOutput output, int parallelism) {
        final var generator = new VectorTileGenerator(output, parallelism);
        generator.generateTiles();
    }

    @Override
    public void generateTiles() {
        output.config().getZoomLevels().stream()
                .map(this::getTileInfos)
                .forEach(this::generateTilesForInfos);

        forkJoinPool.shutdown();
    }

    @NotNull
    private Collection<TileInfo> getTileInfos(final int level) {
        logger.info("Getting tile infos for zoom level {}", level);
        return output.config().getTileLevelsDefinition().tileInfos(level).toList();
    }

    private void generateTilesForInfos(final Collection<TileInfo> tileInfos) {
        final var currentLevel = tileInfos.stream().findFirst().orElseThrow().level();
        final var prefix = String.format("Level %d: ", currentLevel);
        final var progressLogger = new ProgressLogger(prefix, tileInfos.size());

        forkJoinPool
                .submit(
                        () ->
                                tileInfos.parallelStream()
                                        .map(this::createTile)
                                        .map(this::generateTile)
                                        .forEach(progressLogger.watch(this::consumeTile)))
                .join();
    }

    @NotNull
    private VectorTile createTile(final TileInfo tileInfo) {
        final var elementsOnTile = elementsOnTile(tileInfo).toList();
        return new VectorTile(tileInfo, elementsOnTile);
    }

    @NotNull
    private BinaryTile generateTile(final Tile tile) {
        final var encoder = TileEncoderFactory.getEncoder(output.config());
        return encoder.encode(tile);
    }

    private void consumeTile(final BinaryTile binaryTile) {
        Optional.ofNullable(output.tileConsumer())
                .ifPresent(consumer -> consumer.uncheck().accept(binaryTile));
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
