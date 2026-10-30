package ch.ubique.uetlib.geo.coordinate.models;

import static java.lang.Math.*;

import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;

public record IntCoordinate(int x, int y, @Nullable Integer z) {

    public IntCoordinate(final Coordinate coordinate) {
        this(
                toIntExact(round(coordinate.getX())),
                toIntExact(round(coordinate.getY())),
                Double.isFinite(coordinate.getZ()) ? toIntExact(round(coordinate.getZ())) : null);
    }

    public int requireZ() {
        if (z == null) {
            throw new IllegalArgumentException("Z not set for " + this);
        }
        return z;
    }
}
