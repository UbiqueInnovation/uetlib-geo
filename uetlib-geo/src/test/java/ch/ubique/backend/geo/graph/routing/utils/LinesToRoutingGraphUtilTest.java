package ch.ubique.backend.geo.graph.routing.utils;

import com.pivovarit.function.ThrowingFunction;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


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

    @Test
    void testTooManyRoutingVertices() throws ParseException {

        final var wktReader = new WKTReader();

        final var line1 =
                (LineString) wktReader.read("LINESTRING (0 0, 10 0, 20 0, 30 0, 40 0, 50 0)");
        line1.setUserData("SAC");
        final var line2 =
                (LineString)
                        wktReader.read("LINESTRING (0 0, 10 0.8, 20 .2, 30 0.8, 40 0.5, 50 0.8)");
        line2.setUserData("TLM");

        final var graphs = LinesToRoutingGraphUtil.buildGraph(List.of(line1, line2));
        final var completeGraph = graphs.completeGraph();

        Assertions.assertNotNull(completeGraph);

        System.out.println(graphs.completeGraph());
        graphs.completeGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.completeGraph().getDirectedEdges().forEach(System.out::println);

        System.out.println(graphs.routingGraph());
        graphs.routingGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.routingGraph().getDirectedEdges().stream()
                .map(e -> e.toString() + ": " + e.getProperties().originalProperties())
                .forEach(System.out::println);

        Assertions.assertEquals(2, graphs.routingGraph().getVertices().size());
        Assertions.assertEquals(2, graphs.routingGraph().getDirectedEdges().size());
    }
}
