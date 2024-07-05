package ch.ubique.backend.geo.tile.common.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;

public record TileInfo(int x, int y, ZoomLevel zoomLevel, ReferencedEnvelope envelope) {

    public TileInfo(
            final int x,
            final int y,
            final ZoomLevel level,
            final TileLevelsDefinition tileLevelsDefinition) {
        this(x, y, level, tileLevelsDefinition.envelopeOf(level, x, y));
    }

    public String path() {
        return level() + "/" + x + "/" + y + ".pbf";
    }

    public int level() {
        return zoomLevel.level();
    }

    @Override
    public String toString() {
        return zoomLevel.toString() + "_" + x + "_" + y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final TileInfo tileInfo)) {
            return false;
        }

        return new EqualsBuilder()
                .append(x, tileInfo.x)
                .append(y, tileInfo.y)
                .append(level(), tileInfo.level())
                .append(
                        envelope.getCoordinateReferenceSystem(),
                        tileInfo.envelope.getCoordinateReferenceSystem())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(x).append(y).append(level()).toHashCode();
    }
}
