package ch.ubique.backend.geo.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class InterpolationUtils {

    private InterpolationUtils() {}

    public static double interpolate(final double a, final double b, final double t) {
        return (1 - t) * a + t * b;
    }

    /**
     * Bilinear interpolation of parameters u and v inside the bounding box {@code u0v0} to {@code u1v1}.
     *
     * @return the interpolated value
     */
    public static double interpolate(
            final double u0v0,
            final double u0v1,
            final double u1v0,
            final double u1v1,
            final double u,
            final double v) {
        return interpolate(interpolate(u0v0, u0v1, v), interpolate(u1v0, u1v1, v), u);
    }

    /**
     * Interpolates a Z value for a coordinate along a line segment. The Z value of the
     * interpolation point (if any) is ignored. If either segment point is missing Z, returns NaN.
     */
    public static double zInterpolate(final Coordinate coordinate, final LineSegment segment) {
        final var t = segment.projectionFactor(coordinate);
        return interpolate(segment.p0.getZ(), segment.p1.getZ(), t);
    }
}
