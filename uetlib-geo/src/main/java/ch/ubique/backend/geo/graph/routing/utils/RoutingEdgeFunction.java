package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;

import org.locationtech.jts.geom.Coordinate;

public interface RoutingEdgeFunction<T> {

    RoutingCompleteEdge toRoutingEdge(final Coordinate from, final Coordinate to, final T context);
}
