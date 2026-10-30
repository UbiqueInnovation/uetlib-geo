package ch.ubique.uetlib.geo.graph.routing.models;

import ch.ubique.uetlib.geo.graph.routing.protobuf.RoutingGraphTile.SurfaceType;
import org.jspecify.annotations.Nullable;

public record RoutingCompleteEdge(float weight, @Nullable SurfaceType surfaceType)
        implements RoutingEdge {

    public RoutingCompleteEdge withWeight(final float weight) {
        return new RoutingCompleteEdge(weight, surfaceType);
    }
}
