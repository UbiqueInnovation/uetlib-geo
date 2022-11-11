package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoutingGraphUtilTest {

    private record VertexProperties(WGS84Coordinate coordinate) implements CoordinateVertexProperties<WGS84Coordinate> {}

    @Test
    void testSimple() {
        final var testGraph = new Graph<VertexProperties, ObjectUtils.Null>();

        testGraph.addVertex(0, new VertexProperties(new WGS84Coordinate(0, 0)));
        testGraph.addVertex(1, new VertexProperties(new WGS84Coordinate(0, 1)));
        testGraph.addVertex(2, new VertexProperties(new WGS84Coordinate(1, 0)));
        testGraph.addVertex(3, new VertexProperties(new WGS84Coordinate(1, 1)));

        testGraph.getOrAddEdge(0, 1);
        testGraph.getOrAddEdge(1, 2);
        testGraph.getOrAddEdge(2, 3);
        testGraph.getOrAddEdge(3, 0);

        routingGraphTests(testGraph);
    }

    @Test
    void test() {
        final var testGraph = new Graph<VertexProperties, ObjectUtils.Null>();

        testGraph.addVertex(0, new VertexProperties(new WGS84Coordinate(0, 0)));
        testGraph.addVertex(1, new VertexProperties(new WGS84Coordinate(0, 1)));
        testGraph.addVertex(2, new VertexProperties(new WGS84Coordinate(1, 0)));
        testGraph.addVertex(3, new VertexProperties(new WGS84Coordinate(1, 1)));
        testGraph.addVertex(4, new VertexProperties(new WGS84Coordinate(2, 0)));
        testGraph.addVertex(5, new VertexProperties(new WGS84Coordinate(3, 3)));
        testGraph.addVertex(6, new VertexProperties(new WGS84Coordinate(4, 9)));
        testGraph.addVertex(7, new VertexProperties(new WGS84Coordinate(5, 11)));

        testGraph.getOrAddEdge(0, 1);
        testGraph.getOrAddEdge(1, 2);
        testGraph.getOrAddEdge(2, 3);
        testGraph.getOrAddEdge(3, 0);
        testGraph.getOrAddEdge(0, 2);
        testGraph.getOrAddEdge(0, 5);
        testGraph.getOrAddEdge(5, 4);
        testGraph.getOrAddEdge(5, 5);
        testGraph.getOrAddEdge(5, 6);
        testGraph.getOrAddEdge(5, 7);

        routingGraphTests(testGraph);
    }

    private static void routingGraphTests(Graph<VertexProperties, ObjectUtils.Null> testGraph) {
        final var cycles = testGraph.findCycles().size();
        final var routingVerticesByDegree = testGraph.getVertices().stream().filter(v -> v.degree() == 1 || v.degree() > 2).count();

        final var routingGraphs = RoutingGraphUtil.createRoutingGraph(testGraph);
        Assertions.assertEquals(testGraph.getVertices().size(), routingGraphs.completeGraph().getVertices().size());
        Assertions.assertEquals(testGraph.getEdges().size(), routingGraphs.completeGraph().getEdges().size());


        Assertions.assertEquals(cycles + routingVerticesByDegree, routingGraphs.routingGraph().getVertices().size());
        Assertions.assertTrue(cycles <= routingGraphs.routingGraph().getEdges().size());

        final var edgesWithoutRoutingId =
                routingGraphs.completeGraph().getEdges().stream()
                        .filter(e -> e.getProperties().getRoutingEdgeId() == null)
                        .count();
        Assertions.assertEquals(0, edgesWithoutRoutingId, "Expected all edges to have a routing id.");
    }
}
