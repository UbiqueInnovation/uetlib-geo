package ch.uetlib.geo.tile.vector.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ch.uetlib.geo.tile.common.models.TileInfo;
import ch.uetlib.geo.tile.common.models.TileOutputFormat;
import ch.uetlib.geo.tile.common.models.ZoomLevel;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Test;

class BinaryTileTest {

    private static final TileInfo TILE_INFO =
            new TileInfo(1, 2, new ZoomLevel(3, 8, 8, 1, 1), new ReferencedEnvelope());

    @Test
    void equalsComparesTheBytesByContent() {
        final var tile = new BinaryTile(TILE_INFO, TileOutputFormat.PBF, new byte[] {1, 2, 3});

        assertEquals(tile, new BinaryTile(TILE_INFO, TileOutputFormat.PBF, new byte[] {1, 2, 3}));
        assertEquals(
                tile.hashCode(),
                new BinaryTile(TILE_INFO, TileOutputFormat.PBF, new byte[] {1, 2, 3}).hashCode());
        assertNotEquals(tile, new BinaryTile(TILE_INFO, TileOutputFormat.PBF, new byte[] {1, 2}));
        assertNotEquals(
                tile, new BinaryTile(TILE_INFO, TileOutputFormat.PNG, new byte[] {1, 2, 3}));
        assertNotEquals(tile, "tile");
    }
}
