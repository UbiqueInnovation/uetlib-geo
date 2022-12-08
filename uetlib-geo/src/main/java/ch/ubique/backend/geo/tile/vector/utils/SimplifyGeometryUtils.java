package ch.ubique.backend.geo.tile.vector.utils;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.shape.CubicBezierCurve;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

public class SimplifyGeometryUtils {

    private SimplifyGeometryUtils() {}

    public static Geometry simplify(final Geometry geometry, double tolerance) {
        return DouglasPeuckerSimplifier.simplify(geometry, tolerance);
    }

    public static Geometry smooth(final Geometry geometry, double alpha) {
        return CubicBezierCurve.bezierCurve(geometry, alpha);
    }

}
