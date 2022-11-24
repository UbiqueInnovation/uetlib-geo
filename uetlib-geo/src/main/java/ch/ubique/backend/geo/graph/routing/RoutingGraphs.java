package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import org.locationtech.jts.geom.Coordinate;

public record RoutingGraphs<T>(
        Graph<Coordinate, T> completeGraph,
        Graph<Coordinate, RoutingGraphEdgeProperties<T>> routingGraph
) {

    public static <S> RoutingGraphs<S> empty() {
        return new RoutingGraphs<>(new Graph<>(), new Graph<>());
    }
}
