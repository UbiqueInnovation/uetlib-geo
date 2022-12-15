package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import java.util.Collection;

public final class VectorTile extends Tile {

    private final Collection<VectorTileElement> elements;

    public VectorTile(final TileInfo tileInfo, final Collection<VectorTileElement> elements) {
        super(tileInfo);
        this.elements = elements;
    }

    public Collection<VectorTileElement> elements() {
        return elements;
    }
}
