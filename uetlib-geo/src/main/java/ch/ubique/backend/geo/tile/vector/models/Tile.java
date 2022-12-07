package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import java.util.Collection;
import org.geotools.geometry.jts.ReferencedEnvelope;

public record Tile(
        TileInfo tileInfo, ReferencedEnvelope boundingBox, Collection<VectorTileElement> elements) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var tile = (Tile) o;
        return boundingBox.equals(tile.boundingBox);
    }

    @Override
    public int hashCode() {
        return boundingBox.hashCode();
    }
}
