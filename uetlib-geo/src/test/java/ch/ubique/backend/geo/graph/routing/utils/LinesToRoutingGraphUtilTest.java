package ch.ubique.backend.geo.graph.routing.utils;

import com.pivovarit.function.ThrowingFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;

import java.util.stream.Stream;


class LinesToRoutingGraphUtilTest {

    @Test
    void test() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines = Stream.of(
                        "LINESTRING (50 0, 50 49.5)",
                        "LINESTRING (0 50, 100 50)"
                )
                .map(ThrowingFunction.unchecked(wktReader::read))
                .map(LineString.class::cast)
                .map(line -> new LineWithInfo<>(line, testInfo))
                .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines);
        final var completeGraph = graphs.completeGraph();

        Assertions.assertNotNull(completeGraph);
        Assertions.assertEquals(4, completeGraph.getVertices().size());
        Assertions.assertEquals(2 * 3, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(completeGraph.getDirectedEdges().stream().allMatch(e -> testInfo.equals(e.getProperties())));
    }

    @Test
    void testDuplicateLines() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines = Stream.of(
                        "LINESTRING (0.1 0, 100 99.4)",
                        "LINESTRING (0 0.3, 100 100.2)",
                        "LINESTRING (0.4 0, 100 100.4)",
                        "LINESTRING (0 0.6, 100.2 100)"
                )
                .map(ThrowingFunction.unchecked(wktReader::read))
                .map(LineString.class::cast)
                .map(line -> new LineWithInfo<>(line, testInfo))
                .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines);
        final var completeGraph = graphs.completeGraph();
        final var routingGraph = graphs.routingGraph();

        Assertions.assertNotNull(completeGraph);
        Assertions.assertEquals(2, completeGraph.getVertices().size());
        Assertions.assertEquals(2, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(completeGraph.getDirectedEdges().stream().allMatch(e -> testInfo.equals(e.getProperties())));

        Assertions.assertTrue(completeGraph.getVertices().stream().allMatch(v -> Double.isNaN(v.getProperties().getZ())));
        Assertions.assertTrue(routingGraph.getVertices().stream().allMatch(v -> Double.isNaN(v.getProperties().getZ())));
    }

    @Test
    void testLineMerging() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines = Stream.of(
                        "LINESTRING (0 0 0, 0 10 10)",
                        "LINESTRING (0 10 10, 0 19.1 20)",
                        "LINESTRING (0 20 20, 0 29.5 30)",
                        "LINESTRING (0 30 30, 0 40 40)"
                )
                .map(ThrowingFunction.unchecked(wktReader::read))
                .map(LineString.class::cast)
                .map(line -> new LineWithInfo<>(line, testInfo))
                .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines);
        final var completeGraph = graphs.completeGraph();
        final var routingGraph = graphs.routingGraph();

        Assertions.assertNotNull(completeGraph);
        Assertions.assertEquals(5, completeGraph.getVertices().size());
        Assertions.assertEquals(2 * 4, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(completeGraph.getDirectedEdges().stream().allMatch(e -> testInfo.equals(e.getProperties())));

        Assertions.assertNotNull(routingGraph);
        Assertions.assertEquals(2, routingGraph.getVertices().size());
        Assertions.assertEquals(2, routingGraph.getDirectedEdges().size()); // Two edges -> one directed edge for each direction
        Assertions.assertTrue(routingGraph.getDirectedEdges().stream().allMatch(e -> testInfo.equals(e.getProperties().originalProperties())));

        Assertions.assertTrue(completeGraph.getVertices().stream().noneMatch(v -> Double.isNaN(v.getProperties().getZ())));
        Assertions.assertTrue(routingGraph.getVertices().stream().noneMatch(v -> Double.isNaN(v.getProperties().getZ())));
    }
}
