package ch.ubique.uetlib.geo.tile.processing.stats;

import ch.ubique.uetlib.core.utils.StatisticUtils;
import ch.ubique.uetlib.geo.tile.common.models.TileInfo;
import ch.ubique.uetlib.geo.tile.vector.models.BinaryTile;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileSizeStatsUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<TileInfo, Double> tileSizes = new ConcurrentHashMap<>();

    public void addMeasurement(final TileInfo tileInfo, final int size) {
        tileSizes.put(tileInfo, (double) size);
    }

    public void addMeasurement(final BinaryTile binaryTile) {
        tileSizes.put(binaryTile.info(), (double) binaryTile.bytes().length);
    }

    public void logStats() {
        tileSizes.keySet().stream()
                .mapToInt(TileInfo::level)
                .distinct()
                .sorted()
                .forEach(this::logStatsForLevel);
        logStatsForCollection("All levels", tileSizes.values());
        logInfosForLargestTiles();
    }

    public void logInfosForLargestTiles() {
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

    public void logStatsForLevel(final Integer level) {
        final var sizesForLevel =
                tileSizes.entrySet().stream()
                        .filter(entry -> entry.getKey().level() == level)
                        .map(Entry::getValue)
                        .toList();
        logStatsForCollection("Level " + level, sizesForLevel);
    }

    public void logStatsForCollection(final String prefix, final Collection<Double> sizes) {
        final var nonEmptyTiles = sizes.stream().filter(size -> size > 0).toList();
        final var numTiles = sizes.size();
        final var numNonEmptyTiles = nonEmptyTiles.size();
        final var nonEmptyPercentage = Math.round(100.0 * numNonEmptyTiles / numTiles);

        if (nonEmptyTiles.isEmpty()) {
            logger.info(
                    "{}: {} tiles in total, of which {} ({} %) are non empty.",
                    prefix, numTiles, numNonEmptyTiles, nonEmptyPercentage);
        } else {
            final var stats = StatisticUtils.formatByteStats(StatisticUtils.stats(nonEmptyTiles));
            logger.info(
                    "{}: {} tiles in total, of which {} ({} %) are non empty, stats for non empty tiles: {}",
                    prefix, numTiles, numNonEmptyTiles, nonEmptyPercentage, stats);
        }
    }
}
