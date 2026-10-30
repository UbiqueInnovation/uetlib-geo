package ch.ubique.uetlib.geo.tile.vector.mapbox.commands;

import org.jspecify.annotations.Nullable;

public record Delta(int dx, int dy, @Nullable Integer dz) {

    public Delta(final int dx, final int dy) {
        this(dx, dy, null);
    }
}
