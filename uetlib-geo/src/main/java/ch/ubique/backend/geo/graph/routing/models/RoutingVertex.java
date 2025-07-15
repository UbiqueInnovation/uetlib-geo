package ch.ubique.backend.geo.graph.routing.models;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Coordinate;

public record RoutingVertex(@NotNull Coordinate coordinate, boolean isHighway) {
    public RoutingVertex copy() {
        return new RoutingVertex(coordinate, isHighway);
    }

    public RoutingVertex update(final boolean isHighway) {
        return new RoutingVertex(coordinate, this.isHighway || isHighway);
    }
}
