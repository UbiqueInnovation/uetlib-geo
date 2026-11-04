package ch.uetlib.geo.tile.common.encoder;

import ch.uetlib.geo.tile.common.models.Tile;
import ch.uetlib.geo.tile.vector.models.BinaryTile;

public interface TileEncoder {

    BinaryTile encode(final Tile tile);
}
