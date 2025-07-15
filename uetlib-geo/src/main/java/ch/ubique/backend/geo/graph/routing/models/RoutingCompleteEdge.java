package ch.ubique.backend.geo.graph.routing.models;

import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile.SurfaceType;

import jakarta.annotation.Nullable;

public record RoutingCompleteEdge(float weight, @Nullable SurfaceType surfaceType)
        implements RoutingEdge {

    public RoutingCompleteEdge withWeight(final float weight) {
        return new RoutingCompleteEdge(weight, surfaceType);
    }
}
