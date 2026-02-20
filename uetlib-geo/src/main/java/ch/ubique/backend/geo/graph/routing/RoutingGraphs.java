package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;

public record RoutingGraphs(
        Graph<RoutingVertex, RoutingCompleteEdge> completeGraph,
        Graph<RoutingVertex, RoutingShortcutEdge> routingGraph) {

    public static RoutingGraphs empty() {
        return new RoutingGraphs(new Graph<>(), new Graph<>());
    }
}
