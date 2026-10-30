package ch.ubique.uetlib.geo.tile.common.encoder;

import ch.ubique.uetlib.geo.tile.common.models.Tile;
import ch.ubique.uetlib.geo.tile.vector.models.BinaryTile;

public interface TileEncoder {

    BinaryTile encode(final Tile tile);
}
