package ch.ubique.backend.geo.tile.common.encoder;

import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

public interface TileEncoder {

    BinaryTile encode(final Tile tile);
}
