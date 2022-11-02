package ch.ubique.backend.geo.tile.common.models;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class TileLevelsDefinitionTest {

    @Test
    void testChildren() {
        final var gridInfo = SwissConstants.swissTileDefinition().onlyWithLevels(List.of(8, 9, 10, 12));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(9)),
                new TileInfo(1, 0, gridInfo.getZoomLevel(9))
        ), gridInfo.childrenTileInfos(new TileInfo(0, 0, gridInfo.getZoomLevel(8))));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(12)),
                new TileInfo(0, 1, gridInfo.getZoomLevel(12)),
                new TileInfo(1, 0, gridInfo.getZoomLevel(12)),
                new TileInfo(1, 1, gridInfo.getZoomLevel(12))
        ), gridInfo.childrenTileInfos(new TileInfo(0, 0, gridInfo.getZoomLevel(10))));

        Assertions.assertEquals(Set.of(
                new TileInfo(1, 0, gridInfo.getZoomLevel(12)),
                new TileInfo(1, 1, gridInfo.getZoomLevel(12))
        ), gridInfo.childrenTileInfos(new TileInfo(1, 0, gridInfo.getZoomLevel(10))));
    }

    @Test
    void testParent() {
        final var gridInfo = SwissConstants.swissTileDefinition()
                .onlyWithLevels(List.of(8, 9, 10, 12, 14, 15));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(10))
        ), gridInfo.parentTileInfos(new TileInfo(0, 0, gridInfo.getZoomLevel(12))));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(10))
        ), gridInfo.parentTileInfos(new TileInfo(0, 1, gridInfo.getZoomLevel(12))));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(10)),
                new TileInfo(1, 0, gridInfo.getZoomLevel(10))
        ), gridInfo.parentTileInfos(new TileInfo(1, 0, gridInfo.getZoomLevel(12))));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(10)),
                new TileInfo(1, 0, gridInfo.getZoomLevel(10))
        ), gridInfo.parentTileInfos(new TileInfo(1, 1, gridInfo.getZoomLevel(12))));

        Assertions.assertEquals(Set.of(
                new TileInfo(0, 0, gridInfo.getZoomLevel(14))
        ), gridInfo.parentTileInfos(new TileInfo(0, 0, gridInfo.getZoomLevel(15))));

        Assertions.assertEquals(Set.of(
                new TileInfo(1, 0, gridInfo.getZoomLevel(14)),
                new TileInfo(1, 1, gridInfo.getZoomLevel(14)),
                new TileInfo(2, 0, gridInfo.getZoomLevel(14)),
                new TileInfo(2, 1, gridInfo.getZoomLevel(14))
        ), gridInfo.parentTileInfos(new TileInfo(2, 1, gridInfo.getZoomLevel(15))));

        Assertions.assertEquals(Set.of(
                new TileInfo(2, 1, gridInfo.getZoomLevel(14))
        ), gridInfo.parentTileInfos(new TileInfo(3, 2, gridInfo.getZoomLevel(15))));
    }

    @Test
    void testCustomZoomLevel() {
        final var gridInfo = SwissConstants.swissTileDefinition()
                .onlyWithLevels(List.of(8, 9, 10, 11, 12))
                .addLevel(new ZoomLevel(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1000));

        final var testTileInfo = new TileInfo(1, 1, gridInfo.getZoomLevel(12));

        for (final var tileInfo : gridInfo.childrenTileInfos(testTileInfo)) {
            if (!gridInfo.parentTileInfos(tileInfo).contains(testTileInfo)) {
                Assertions.fail();
            }
        }
    }
}
