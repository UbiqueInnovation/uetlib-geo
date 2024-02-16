package ch.ubique.backend.geo.graph.routing.models;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Coordinate;

public record RoutingGraphVertexProperties(@NotNull Coordinate coordinate, boolean isHighway) {
    public RoutingGraphVertexProperties copy() {
        return new RoutingGraphVertexProperties(coordinate, isHighway);
    }

    public RoutingGraphVertexProperties update(final boolean isHighway) {
        return new RoutingGraphVertexProperties(coordinate, this.isHighway || isHighway);
    }
}
