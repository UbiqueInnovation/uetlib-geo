package ch.uetlib.geo.tile.vector.models;

import ch.uetlib.geo.tile.common.models.Tile;
import ch.uetlib.geo.tile.common.models.TileInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;

public final class VectorTile extends Tile {

    @JsonProperty private final Collection<VectorTileElement> elements;

    public VectorTile(final TileInfo tileInfo, final Collection<VectorTileElement> elements) {
        super(tileInfo);
        this.elements = elements;
    }

    public Collection<VectorTileElement> elements() {
        return elements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final VectorTile that)) {
            return false;
        }

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(elements, that.elements)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(elements)
                .toHashCode();
    }
}
