package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import java.util.Collection;
import org.geotools.geometry.jts.ReferencedEnvelope;

public final class VectorTile extends Tile {

    private final Collection<VectorTileElement> elements;

    public VectorTile(
            final TileInfo tileInfo,
            final ReferencedEnvelope boundingBox,
            final Collection<VectorTileElement> elements) {
        super(tileInfo, boundingBox);
        this.elements = elements;
    }

    public Collection<VectorTileElement> elements() {
        return elements;
    }
}
