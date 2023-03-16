package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import org.locationtech.jts.geom.Coordinate;

public record RoutingGraphs(
        Graph<Coordinate, Weight> completeGraph,
        Graph<Coordinate, WeightAndDirection> routingGraph) {

    public static RoutingGraphs empty() {
        return new RoutingGraphs(new Graph<>(), new Graph<>());
    }
}
