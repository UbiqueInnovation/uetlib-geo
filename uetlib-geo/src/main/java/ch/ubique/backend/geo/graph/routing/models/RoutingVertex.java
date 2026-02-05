package ch.ubique.backend.geo.graph.routing.models;

import org.jspecify.annotations.NonNull;

import org.locationtech.jts.geom.Coordinate;

public record RoutingVertex(@NonNull Coordinate coordinate, boolean isHighway) {
    public RoutingVertex copy() {
        return new RoutingVertex(coordinate, isHighway);
    }

    public RoutingVertex update(final boolean isHighway) {
        return new RoutingVertex(coordinate, this.isHighway || isHighway);
    }
}
