package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import jakarta.validation.constraints.NotNull;

public record RoutingGraphs(
        @NotNull Graph<RoutingGraphVertexProperties, Weight> completeGraph,
        @NotNull Graph<RoutingGraphVertexProperties, WeightAndDirection> routingGraph) {

    public static RoutingGraphs empty() {
        return new RoutingGraphs(new Graph<>(), new Graph<>());
    }
}
