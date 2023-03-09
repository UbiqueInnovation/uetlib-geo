package ch.ubique.backend.geo.tile.common.models;

import static ch.ubique.backend.geo.coordinate.util.JtsUtil.getCrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class TileLevelsDefinitionTest {

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
    void testOutputBoundingBox() {
        final ReferencedEnvelope chEnvelope =
                SwissConstants.swissTileDefinition().tileInfo(0, 0, 9).envelope();
        final List<TileInfo> tileInfos =
                WebMercatorTileDefinitions.tileDefinitions()
                        .tileInfos(7, chEnvelope)
                        .collect(Collectors.toList());
        assertEquals(9, tileInfos.size());
        for (int x = 65; x <= 67; x++) {
            for (int y = 44; y <= 46; y++) {
                int finalX = x;
                int finalY = y;
                assertTrue(
                        tileInfos.stream()
                                .filter(
                                        tileInfo ->
                                                tileInfo.x() == finalX && tileInfo.y() == finalY)
                                .findAny()
                                .isPresent());
            }
        }

        final ReferencedEnvelope closerChEnvelope =
                new ReferencedEnvelope(
                        new Envelope(2470000, 2880000, 1033000, 1310000), getCrs(2056));
        final List<TileInfo> tileInfos2 =
                WebMercatorTileDefinitions.tileDefinitions()
                        .tileInfos(7, closerChEnvelope)
                        .collect(Collectors.toList());
        assertEquals(4, tileInfos2.size());
        for (int x = 66; x <= 67; x++) {
            for (int y = 44; y <= 45; y++) {
                int finalX = x;
                int finalY = y;
                assertTrue(
                        tileInfos2.stream()
                                .filter(
                                        tileInfo ->
                                                tileInfo.x() == finalX && tileInfo.y() == finalY)
                                .findAny()
                                .isPresent());
            }
        }
    }
}
