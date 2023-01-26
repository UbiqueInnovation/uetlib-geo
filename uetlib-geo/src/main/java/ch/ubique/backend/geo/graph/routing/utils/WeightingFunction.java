package ch.ubique.backend.geo.graph.routing.utils;

import org.locationtech.jts.geom.Coordinate;

public interface WeightingFunction<T> {

    float computeWeight(final Coordinate from, final Coordinate to, final T context);
}
