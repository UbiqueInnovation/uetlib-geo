package ch.ubique.backend.geo.tile.vector.utils;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

public class SimplifyGeometryUtils {

    private SimplifyGeometryUtils() {}

    public static Geometry simplify(final Geometry geometry, double tolerance) {
        return TopologyPreservingSimplifier.simplify(geometry, tolerance);
    }

}
