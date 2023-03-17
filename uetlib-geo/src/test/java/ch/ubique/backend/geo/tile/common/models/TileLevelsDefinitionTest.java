package ch.ubique.backend.geo.tile.common.models;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class TileLevelsDefinitionTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void testChildren() {
        final var gridInfo =
                SwissConstants.swissTileDefinition().onlyWithLevels(List.of(8, 9, 10, 12));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 9), gridInfo.tileInfo(1, 0, 9)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(0, 0, 8)));

        assertEquals(
                Set.of(
                        gridInfo.tileInfo(0, 0, 12),
                        gridInfo.tileInfo(0, 1, 12),
                        gridInfo.tileInfo(1, 0, 12),
                        gridInfo.tileInfo(1, 1, 12)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(0, 0, 10)));

        assertEquals(
                Set.of(gridInfo.tileInfo(1, 0, 12), gridInfo.tileInfo(1, 1, 12)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(1, 0, 10)));
    }

    @Test
    void testParent() {
        final var gridInfo =
                SwissConstants.swissTileDefinition().onlyWithLevels(List.of(8, 9, 10, 12, 14, 15));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 0, 12)));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 1, 12)));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10), gridInfo.tileInfo(1, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(1, 0, 12)));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10), gridInfo.tileInfo(1, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(1, 1, 12)));

        assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 14)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 0, 15)));

        assertEquals(
                Set.of(
                        gridInfo.tileInfo(1, 0, 14),
                        gridInfo.tileInfo(1, 1, 14),
                        gridInfo.tileInfo(2, 0, 14),
                        gridInfo.tileInfo(2, 1, 14)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(2, 1, 15)));

        assertEquals(
                Set.of(gridInfo.tileInfo(2, 1, 14)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(3, 2, 15)));
    }

    @Test
    void testCustomZoomLevel() {
        final var gridInfo =
                SwissConstants.swissTileDefinition()
                        .onlyWithLevels(List.of(8, 9, 10, 11, 12))
                        .addLevel(
                                new ZoomLevel(
                                        Integer.MAX_VALUE,
                                        Integer.MAX_VALUE,
                                        Integer.MAX_VALUE,
                                        1000,
                                        1000));

        final var testTileInfo = gridInfo.tileInfo(1, 1, 12);

        for (final var tileInfo : gridInfo.childrenTileInfos(testTileInfo)) {
            if (!gridInfo.parentTileInfos(tileInfo).contains(testTileInfo)) {
                Assertions.fail();
            }
        }
    }

    @Test
    void testOutputBoundingBoxExamples() {
        final ReferencedEnvelope chEnvelope =
                SwissConstants.swissTileDefinition().tileInfo(0, 0, 9).envelope();
        final List<TileInfo> tileInfos =
                WebMercatorTileDefinitions.tileDefinitions().tileInfos(7, chEnvelope).toList();
        assertEquals(9, tileInfos.size());
        for (int x = 65; x <= 67; x++) {
            for (int y = 44; y <= 46; y++) {
                final int finalX = x;
                final int finalY = y;
                assertTrue(
                        tileInfos.stream()
                                .anyMatch(
                                        tileInfo ->
                                                tileInfo.x() == finalX && tileInfo.y() == finalY));
            }
        }

        final ReferencedEnvelope closerChEnvelope =
                new ReferencedEnvelope(
                        new Envelope(2470000, 2880000, 1033000, 1310000), getCrs(2056));
        final List<TileInfo> tileInfos2 =
                WebMercatorTileDefinitions.tileDefinitions()
                        .tileInfos(7, closerChEnvelope)
                        .toList();
        assertEquals(4, tileInfos2.size());
        for (int x = 66; x <= 67; x++) {
            for (int y = 44; y <= 45; y++) {
                final int finalX = x;
                final int finalY = y;
                assertTrue(
                        tileInfos2.stream()
                                .anyMatch(
                                        tileInfo ->
                                                tileInfo.x() == finalX && tileInfo.y() == finalY));
            }
        }
    }

    @Test
    void testOutputBoundingBoxLV95Bounds() throws FactoryException, TransformException {
        final var chEnvelope = SwissConstants.swissTileDefinition().tileInfo(0, 0, 9).envelope();
        final var webMercatorChEnvelope =
                chEnvelope.transform(JtsConverter.WEB_MERCATOR.crs(), false);

        // Test from zoom level 0 (one tile for entire world) to 13 (size of Oerlikon)
        for (final var level : IntStream.rangeClosed(0, 13).toArray()) {

            logger.info("Testing level {}", level);

            final var expected =
                    WebMercatorTileDefinitions.tileDefinitions()
                            .tileInfos(level)
                            .filter(
                                    info ->
                                            info.envelope()
                                                    .intersects((Envelope) webMercatorChEnvelope))
                            .map(TileInfo::toString)
                            .collect(Collectors.toUnmodifiableSet());

            final var actual =
                    WebMercatorTileDefinitions.tileDefinitions()
                            .tileInfos(level, chEnvelope)
                            .map(TileInfo::toString)
                            .collect(Collectors.toUnmodifiableSet());

            assertEquals(expected, actual);
        }
    }

    @Test
    void testOutputBoundingBoxWebmercatorBounds() {
        final var tileDefinitions = WebMercatorTileDefinitions.tileDefinitions();

        final var x0 = 33;
        final var y0 = 22;
        final var z0 = 6;
        final var chEnvelope = tileDefinitions.tileInfo(x0, y0, z0).envelope();

        // Test from zoom level 0 (one tile for entire world) to 11 (size of Zürich)
        for (final var level : IntStream.rangeClosed(z0, 11).toArray()) {

            logger.info("Testing level {}", level);

            final var tilesPerSide = (int) Math.pow(2, level - z0);

            final var expected =
                    IntStream.range(x0 * tilesPerSide, (x0 + 1) * tilesPerSide)
                            .mapToObj(
                                    x ->
                                            IntStream.range(
                                                            y0 * tilesPerSide,
                                                            (y0 + 1) * tilesPerSide)
                                                    .mapToObj(
                                                            y ->
                                                                    tileDefinitions.tileInfo(
                                                                            x, y, level)))
                            .flatMap(Function.identity())
                            .map(TileInfo::toString)
                            .sorted()
                            .toList();

            final var actual =
                    tileDefinitions
                            .tileInfos(level, chEnvelope)
                            .map(TileInfo::toString)
                            .sorted()
                            .toList();

            assertEquals(expected, actual);
        }
    }
}
