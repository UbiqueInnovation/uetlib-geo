package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.core.utils.ProgressLogger;
import ch.ubique.backend.core.utils.StatisticUtils;
import ch.ubique.backend.geo.tile.common.encoder.TileEncoderFactory;
import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.utils.TileGenerator;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VectorTileGenerator implements TileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileGenerator.class);

    private final VectorTileOutput output;

    private final ExecutorService executorService;
    private final Cache<VectorTileElement, Envelope> envelopeCache;

    private final Map<TileInfo, Double> tileSizes;

    public VectorTileGenerator(final VectorTileOutput output, final int parallelism) {
        this.output = output;
        final var threadFactory =
                new ThreadFactoryBuilder().setNameFormat("vector-tile-%d").build();
        this.executorService = Executors.newFixedThreadPool(parallelism, threadFactory);
        final var cacheBuilder = Caffeine.newBuilder();
        this.envelopeCache = cacheBuilder.build();
        this.tileSizes = new ConcurrentHashMap<>();
    }

    public static void generateTiles(final VectorTileOutput output) {
        generateTiles(output, 64);
    }

    public static void generateTiles(final VectorTileOutput output, final int parallelism) {
        final var generator = new VectorTileGenerator(output, parallelism);
        generator.generateTiles();
    }

    @Override
    public void generateTiles() {
        logger.info("Generating tiles now.");
        output.config().zoomLevels().stream()
                .sorted()
                .map(z -> getTileInfos(z, output.config().outputEnvelopes()))
                .forEach(this::submitTasksToGenerateTilesForInfos);
        logger.info("Submitted tasks for all levels.");

        waitForCompletion();

        logStats();

        envelopeCache.invalidateAll();
    }

    @NotNull
    private Collection<TileInfo> getTileInfos(
            final int level, final Collection<ReferencedEnvelope> outputEnvelopes) {
        return output.config().tileLevelsDefinition().tileInfos(level, outputEnvelopes).toList();
    }

    private void submitTasksToGenerateTilesForInfos(final Collection<TileInfo> tileInfos) {
        final var currentLevel = tileInfos.stream().findFirst().orElseThrow().level();
        logger.info("Submitting tasks for {} tiles of level {}.", tileInfos.size(), currentLevel);
        final var prefix = String.format("Level %d: ", currentLevel);
        final var mdcContextMap = MDC.getCopyOfContextMap();
        final var progressLogger = new ProgressLogger(prefix, tileInfos.size(), mdcContextMap);

        for (final var tileInfo : tileInfos) {
            executorService.submit(
                    Executors.callable(
                            () -> {
                                createGenerateAndConsumeTile(tileInfo);
                                progressLogger.step();
                            }));
        }
    }

    private void logStats() {
        output.config().zoomLevels().stream().sorted().forEach(this::logStatsForLevel);
        logStatsForCollection("All levels", tileSizes.values());
        logInfosForLargestTiles();
    }

    private void logInfosForLargestTiles() {
        logger.info("Finding largest tiles.");
        tileSizes.entrySet().stream()
                .sorted(Comparator.comparing(entry -> -entry.getValue()))
                .limit(10)
                .forEach(
                        entry ->
                                logger.info(
                                        "{}: {}",
                                        entry.getKey().path(),
                                        FileUtils.byteCountToDisplaySize(entry.getValue())));
    }

    private void logStatsForLevel(final Integer level) {
        final var sizesForLevel =
                tileSizes.entrySet().stream()
                        .filter(entry -> entry.getKey().level() == level)
                        .map(Map.Entry::getValue)
                        .toList();
        logStatsForCollection("Level " + level, sizesForLevel);
    }

    private void logStatsForCollection(final String prefix, final Collection<Double> sizes) {
        final var nonEmptyTiles = sizes.stream().filter(size -> size > 0).toList();
        final var numTiles = sizes.size();
        final var numNonEmptyTiles = nonEmptyTiles.size();
        final var nonEmptyPercentage = Math.round(100.0 * numNonEmptyTiles / numTiles);
        logger.info(
                "{}: {} tiles in total, of which {} ({} %) are non empty.",
                prefix, numTiles, numNonEmptyTiles, nonEmptyPercentage);

        if (!nonEmptyTiles.isEmpty()) {
            final var stats = StatisticUtils.formatByteStats(StatisticUtils.stats(nonEmptyTiles));
            logger.info("{}: stats for non empty tiles: {}", prefix, stats);
        }
    }

    private void createGenerateAndConsumeTile(final TileInfo tileInfo) {
        try {
            final var tile = createTile(tileInfo);
            final var binaryTile = generateTile(tile);
            tileSizes.put(binaryTile.info(), (double) binaryTile.bytes().length);
            consumeTile(binaryTile);
        } catch (final Exception e) {
            logger.warn("Could not create tile {}.", tileInfo, e);
        }
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
        final var level = tileInfo.level();
        final var tileEnvelope = tileInfo.envelope();
        final var envelopeGeometry = JTS.toGeometry(tileEnvelope);

        final var elementsOnTile =
                output.elements(tileInfo).stream()
                        .filter(isOnTile(level, tileEnvelope, envelopeGeometry));

        final var limit = output.config().getLimit(tileInfo.level());
        if (limit.isEmpty()) {
            return elementsOnTile;
        }

        return elementsOnTile
                .sorted(Comparator.comparingDouble(VectorTileElement::score).reversed())
                .limit(limit.get());
    }

    private Predicate<VectorTileElement> isOnTile(
            final int level,
            final ReferencedEnvelope tileEnvelope,
            final Polygon envelopeGeometry) {

        return element -> {
            TileGeometryUtils.ensureCrsMatch(element.geometry(), tileEnvelope);

            if (!element.showElement(level)) {
                return false;
            }

            if (element.geometry() instanceof final Point point) {
                return tileEnvelope.covers(point.getCoordinate());
            }

            final var elementEnvelope = envelopeCache.get(element, VectorTileElement::boundingBox);
            return tileEnvelope.intersects(elementEnvelope)
                    && envelopeGeometry.intersects(element.geometry());
        };
    }

    private void waitForCompletion() {
        try {
            executorService.shutdown();
            final var terminatedInTime = executorService.awaitTermination(14, TimeUnit.DAYS);
            if (!terminatedInTime) {
                throw new IllegalStateException("Timout elapsed before termination.");
            }
            logger.info("Generated all tiles.");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
