package ch.ubique.backend.geo.tile.smartdrawing.models;

import ch.ubique.backend.geo.coordinate.models.Coordinate;

import org.jspecify.annotations.Nullable;

public record SmartDrawingVertex(long id, Coordinate coordinate) {

    @Override
    public boolean equals(final @Nullable Object o) {
        return o == this || (o instanceof final SmartDrawingVertex that && id == that.id);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
