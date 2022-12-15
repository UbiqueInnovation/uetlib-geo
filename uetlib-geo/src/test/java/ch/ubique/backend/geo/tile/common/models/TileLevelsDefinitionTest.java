package ch.ubique.backend.geo.tile.common.models;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TileLevelsDefinitionTest {

    @Test
    void testChildren() {
        final var gridInfo =
                SwissConstants.swissTileDefinition().onlyWithLevels(List.of(8, 9, 10, 12));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 9), gridInfo.tileInfo(1, 0, 9)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(0, 0, 8)));

        Assertions.assertEquals(
                Set.of(
                        gridInfo.tileInfo(0, 0, 12),
                        gridInfo.tileInfo(0, 1, 12),
                        gridInfo.tileInfo(1, 0, 12),
                        gridInfo.tileInfo(1, 1, 12)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(0, 0, 10)));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(1, 0, 12), gridInfo.tileInfo(1, 1, 12)),
                gridInfo.childrenTileInfos(gridInfo.tileInfo(1, 0, 10)));
    }

    @Test
    void testParent() {
        final var gridInfo =
                SwissConstants.swissTileDefinition().onlyWithLevels(List.of(8, 9, 10, 12, 14, 15));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 0, 12)));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 1, 12)));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10), gridInfo.tileInfo(1, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(1, 0, 12)));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 10), gridInfo.tileInfo(1, 0, 10)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(1, 1, 12)));

        Assertions.assertEquals(
                Set.of(gridInfo.tileInfo(0, 0, 14)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(0, 0, 15)));

        Assertions.assertEquals(
                Set.of(
                        gridInfo.tileInfo(1, 0, 14),
                        gridInfo.tileInfo(1, 1, 14),
                        gridInfo.tileInfo(2, 0, 14),
                        gridInfo.tileInfo(2, 1, 14)),
                gridInfo.parentTileInfos(gridInfo.tileInfo(2, 1, 15)));

        Assertions.assertEquals(
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

}
