package ch.uetlib.geo.tile.common.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        return new HashCodeBuilder(17, 37).append(tileInfo).toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final Tile tile)) {
            return false;
        }

        return new EqualsBuilder().append(tileInfo, tile.tileInfo).isEquals();
    }
}
