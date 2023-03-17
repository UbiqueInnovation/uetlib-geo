package ch.ubique.backend.geo.tile.common.models;

public class Tile {

    private final TileInfo tileInfo;

    public Tile(final TileInfo tileInfo) {
        this.tileInfo = tileInfo;
    }

    public TileInfo tileInfo() {
        return tileInfo;
    }

    @Override
    public int hashCode() {
        return tileInfo.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var tile = (Tile) o;
        return tileInfo.equals(tile.tileInfo);
    }
}
