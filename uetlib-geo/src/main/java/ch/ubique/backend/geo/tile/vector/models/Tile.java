package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collection;

public record Tile(
        TileInfo tileInfo,
        Envelope boundingBox,
        Collection<VectorTileElement> elements
) {

    public Tile(TileInfo tileInfo, Envelope boundingBox) {
        this(tileInfo, boundingBox, new ArrayList<>());
    }

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
