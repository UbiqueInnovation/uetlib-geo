package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;

public record RoutingGraphs<V extends CoordinateVertexProperties, E>(
        Graph<V, RoutingLinkEdgeProperties<E>> completeGraph,
        Graph<V, RoutingGraphEdgeProperties<V, E>> routingGraph
) { }
