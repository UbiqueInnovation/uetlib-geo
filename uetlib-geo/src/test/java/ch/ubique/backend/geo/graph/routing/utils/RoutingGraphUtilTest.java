package ch.ubique.backend.geo.graph.routing.utils;

import static java.util.Objects.requireNonNullElseGet;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class RoutingGraphUtilTest {

    private static void routingGraphTests(
            final Graph<RoutingVertex, RoutingCompleteEdge> testGraph) {
        final var cycles = testGraph.findCycles().size();
        final var routingVerticesByDegree =
                testGraph.getVertices().stream()
                        .filter(v -> v.degree() == 1 || v.degree() > 2)
                        .count();

        final var routingGraph = RoutingGraphUtil.createRoutingGraph(testGraph);

        Assertions.assertEquals(
                cycles + routingVerticesByDegree, routingGraph.getVertices().size());
        Assertions.assertTrue(routingVerticesByDegree <= routingGraph.getDirectedEdges().size());
    }

    @Test
    void testSimple() {
        final var testGraph = new Graph<Coordinate, RoutingCompleteEdge>();

        testGraph.addVertex(0, new WGS84Coordinate(0, 0).toJts());
        testGraph.addVertex(1, new WGS84Coordinate(0, 1).toJts());
        testGraph.addVertex(2, new WGS84Coordinate(1, 0).toJts());
        testGraph.addVertex(3, new WGS84Coordinate(1, 1).toJts());

        testGraph.getOrAddUndirectedEdge(0, 1);
        testGraph.getOrAddUndirectedEdge(1, 2);
        testGraph.getOrAddUndirectedEdge(2, 3);
        testGraph.getOrAddUndirectedEdge(3, 0);

        routingGraphTests(toRoutingGraph(testGraph));
    }

    @Test
    void test() {
        final var testGraph = new Graph<Coordinate, RoutingCompleteEdge>();

        testGraph.addVertex(0, new WGS84Coordinate(0, 0).toJts());
        testGraph.addVertex(1, new WGS84Coordinate(0, 1).toJts());
        testGraph.addVertex(2, new WGS84Coordinate(1, 0).toJts());
        testGraph.addVertex(3, new WGS84Coordinate(1, 1).toJts());
        testGraph.addVertex(4, new WGS84Coordinate(2, 0).toJts());
        testGraph.addVertex(5, new WGS84Coordinate(3, 3).toJts());
        testGraph.addVertex(6, new WGS84Coordinate(4, 9).toJts());
        testGraph.addVertex(7, new WGS84Coordinate(5, 11).toJts());

        testGraph.getOrAddUndirectedEdge(0, 1);
        testGraph.getOrAddUndirectedEdge(1, 2);
        testGraph.getOrAddUndirectedEdge(2, 3);
        testGraph.getOrAddUndirectedEdge(3, 0);
        testGraph.getOrAddUndirectedEdge(0, 2);
        testGraph.getOrAddUndirectedEdge(0, 5);
        testGraph.getOrAddUndirectedEdge(5, 4);
        testGraph.getOrAddUndirectedEdge(5, 5);
        testGraph.getOrAddUndirectedEdge(5, 6);
        testGraph.getOrAddUndirectedEdge(5, 7);

        routingGraphTests(toRoutingGraph(testGraph));
    }

    private static Graph<RoutingVertex, RoutingCompleteEdge> toRoutingGraph(
            final Graph<Coordinate, RoutingCompleteEdge> testGraph) {
        return testGraph.map(
                v -> new RoutingVertex(v.getProperties(), false),
                e ->
                        requireNonNullElseGet(
                                e.getProperties(),
                                () ->
                                        new RoutingCompleteEdge(
                                                (float)
                                                        e.getFrom()
                                                                .getProperties()
                                                                .distance(
                                                                        e.getTo().getProperties()),
                                                null)));
    }
}
