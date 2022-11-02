package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

class RasterTileGeneratorTest {

    @Test
    void test() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().withMaxLevel(24);
        final var sourceTileSize = 1000;

        final var generatedTiles = new HashSet<TileInfo>();
        final var queriedSourceTiles = new HashSet<TileInfo>();

        final var generator = new RasterTileGenerator(tileLevelsDefinition, sourceTileSize) {
            @Override
            protected void onTileGenerated(RasterTile rasterTile) {
                Assertions.assertFalse(generatedTiles.contains(rasterTile.tileInfo()));
                generatedTiles.add(rasterTile.tileInfo());
            }

            @Override
            protected RasterTile getSourceTile(TileInfo tileInfo) {
                Assertions.assertFalse(queriedSourceTiles.contains(tileInfo));
                queriedSourceTiles.add(tileInfo);
                return new RasterTile(tileInfo, null);
            }
        };

        generator.generateTiles();

        Assertions.assertFalse(queriedSourceTiles.isEmpty());
        Assertions.assertFalse(generatedTiles.isEmpty());
        Assertions.assertEquals(tileLevelsDefinition.numberOfTiles(), generatedTiles.size());
    }
}
