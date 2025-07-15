package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;
import ch.ubique.backend.geo.graph.routing.models.RoutingShortcutEdge;

import jakarta.validation.constraints.NotNull;

public record RoutingGraphs(
        @NotNull Graph<RoutingVertex, RoutingCompleteEdge> completeGraph,
        @NotNull Graph<RoutingVertex, RoutingShortcutEdge> routingGraph) {

    public static RoutingGraphs empty() {
        return new RoutingGraphs(new Graph<>(), new Graph<>());
    }
}
