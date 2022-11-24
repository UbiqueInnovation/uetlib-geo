package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class RoutingGraphUtilTest {

    private record VertexProperties(WGS84Coordinate coordinate) implements CoordinateVertexProperties<WGS84Coordinate> {}

    @Test
    void testSimple() {
        final var testGraph = new Graph<Coordinate, ObjectUtils.Null>();

        testGraph.addVertex(0, new WGS84Coordinate(0, 0).toJts());
        testGraph.addVertex(1, new WGS84Coordinate(0, 1).toJts());
        testGraph.addVertex(2, new WGS84Coordinate(1, 0).toJts());
        testGraph.addVertex(3, new WGS84Coordinate(1, 1).toJts());

        testGraph.getOrAddUndirectedEdge(0, 1);
        testGraph.getOrAddUndirectedEdge(1, 2);
        testGraph.getOrAddUndirectedEdge(2, 3);
        testGraph.getOrAddUndirectedEdge(3, 0);

        routingGraphTests(testGraph);
    }

    @Test
    void test() {
        final var testGraph = new Graph<Coordinate, ObjectUtils.Null>();

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

        routingGraphTests(testGraph);
    }

    private static void routingGraphTests(Graph<Coordinate, ObjectUtils.Null> testGraph) {
        final var cycles = testGraph.findCycles().size();
        final var routingVerticesByDegree = testGraph.getVertices().stream().filter(v -> v.degree() == 1 || v.degree() > 2).count();

        final var routingGraph = RoutingGraphUtil.createRoutingGraph(testGraph, JtsConverter.WGS84.crs());

        Assertions.assertEquals(cycles + routingVerticesByDegree, routingGraph.getVertices().size());
        Assertions.assertTrue(routingVerticesByDegree <= routingGraph.getDirectedEdges().size());
    }
}
