package ch.ubique.uetlib.geo.graph.routing;

import ch.ubique.uetlib.geo.graph.models.Graph;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingVertex;

public record RoutingGraphs(
        Graph<RoutingVertex, RoutingCompleteEdge> completeGraph,
        Graph<RoutingVertex, RoutingShortcutEdge> routingGraph) {

    public static RoutingGraphs empty() {
        return new RoutingGraphs(new Graph<>(), new Graph<>());
    }
}
