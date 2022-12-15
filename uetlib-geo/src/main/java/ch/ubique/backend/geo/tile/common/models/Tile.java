package ch.ubique.backend.geo.tile.common.models;

import org.geotools.geometry.jts.ReferencedEnvelope;

public class Tile {

    private final TileInfo tileInfo;
    private final ReferencedEnvelope boundingBox;

    public Tile(final TileInfo tileInfo, final ReferencedEnvelope boundingBox) {
        this.tileInfo = tileInfo;
        this.boundingBox = boundingBox;
    }

    public TileInfo tileInfo() {
        return tileInfo;
    }

    public ReferencedEnvelope boundingBox() {
        return boundingBox;
    }

    @Override
    public int hashCode() {
        return boundingBox.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var tile = (Tile) o;
        return boundingBox.equals(tile.boundingBox);
    }
}
