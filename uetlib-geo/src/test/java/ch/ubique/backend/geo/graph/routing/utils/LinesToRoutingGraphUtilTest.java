package ch.ubique.backend.geo.graph.routing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.ubique.backend.geo.graph.algorithms.ConnectedComponents;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;

import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.utils.GeoJsonUtil;
import com.pivovarit.function.ThrowingFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.List;
import java.util.stream.Stream;

class LinesToRoutingGraphUtilTest {
    private final LinesToGraphConfig<String> config =
            LinesToGraphConfig.<String>builder()
                    .routingEdgeFunction(
                            (from, to, info) -> {
                                Assertions.assertNotNull(info);
                                return new RoutingCompleteEdge((float) from.distance(to), null);
                            })
                    .build();

    @Test
    void test() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines =
                Stream.of("LINESTRING (50 0, 50 49.5)", "LINESTRING (0 50, 100 50)")
                        .map(ThrowingFunction.unchecked(wktReader::read))
                        .map(LineString.class::cast)
                        .map(line -> new LineWithInfo<>(line, testInfo))
                        .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines, config);
        final var completeGraph = graphs.completeGraph();

        Assertions.assertNotNull(completeGraph);
        assertEquals(4, completeGraph.getVertices().size());
        assertEquals(2 * 3, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(
                completeGraph.getDirectedEdges().stream()
                        .allMatch(e -> e.getProperties().weight() > 0));

        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream().allMatch(v -> v.degree() != 2));
    }

    @Test
    void testDuplicateLines() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines =
                Stream.of(
                                "LINESTRING (0.1 0, 100 99.4)",
                                "LINESTRING (0 0.3, 100 100.2)",
                                "LINESTRING (0.4 0, 100 100.4)",
                                "LINESTRING (0 0.6, 100.2 100)")
                        .map(ThrowingFunction.unchecked(wktReader::read))
                        .map(LineString.class::cast)
                        .map(line -> new LineWithInfo<>(line, testInfo))
                        .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines, config);
        final var completeGraph = graphs.completeGraph();
        final var routingGraph = graphs.routingGraph();

        Assertions.assertNotNull(completeGraph);
        assertEquals(2, completeGraph.getVertices().size());
        assertEquals(2, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(
                completeGraph.getDirectedEdges().stream()
                        .allMatch(e -> e.getProperties().weight() > 0));

        Assertions.assertTrue(
                completeGraph.getVertices().stream()
                        .allMatch(v -> Double.isNaN(v.getProperties().coordinate().getZ())));
        Assertions.assertTrue(
                routingGraph.getVertices().stream()
                        .allMatch(v -> Double.isNaN(v.getProperties().coordinate().getZ())));

        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream().allMatch(v -> v.degree() != 2));
    }

    @Test
    void testLineMerging() {

        final var wktReader = new WKTReader();
        final var testInfo = "Hello there";

        final var lines =
                Stream.of(
                                "LINESTRING (0 0 0, 0 10 10)",
                                "LINESTRING (0 10 10, 0 19.1 20)",
                                "LINESTRING (0 20 20, 0 29.5 30)",
                                "LINESTRING (0 30 30, 0 40 40)")
                        .map(ThrowingFunction.unchecked(wktReader::read))
                        .map(LineString.class::cast)
                        .map(line -> new LineWithInfo<>(line, testInfo))
                        .toList();

        final var graphs = LinesToRoutingGraphUtil.buildGraphWithInfo(lines, config);
        final var completeGraph = graphs.completeGraph();
        final var routingGraph = graphs.routingGraph();

        Assertions.assertNotNull(completeGraph);
        assertEquals(5, completeGraph.getVertices().size());
        assertEquals(2 * 4, completeGraph.getDirectedEdges().size());
        Assertions.assertTrue(
                completeGraph.getDirectedEdges().stream()
                        .allMatch(e -> e.getProperties().weight() > 0));

        Assertions.assertNotNull(routingGraph);
        assertEquals(2, routingGraph.getVertices().size());
        assertEquals(
                2,
                routingGraph
                        .getDirectedEdges()
                        .size()); // Two edges -> one directed edge for each direction
        Assertions.assertTrue(
                routingGraph.getDirectedEdges().stream()
                        .allMatch(e -> e.getProperties().weight() > 0));

        Assertions.assertTrue(
                completeGraph.getVertices().stream()
                        .noneMatch(v -> Double.isNaN(v.getProperties().coordinate().getZ())));
        Assertions.assertTrue(
                routingGraph.getVertices().stream()
                        .noneMatch(v -> Double.isNaN(v.getProperties().coordinate().getZ())));
        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream().allMatch(v -> v.degree() != 2));
    }

    @Test
    void testTooManyRoutingVertices() throws ParseException {

        final var wktReader = new WKTReader();

        final var line1 =
                (LineString) wktReader.read("LINESTRING (0 0, 10 0, 20 0, 30 0, 40 0, 50 0)");
        line1.setUserData("SAC");
        final var line2 =
                (LineString)
                        wktReader.read(
                                "LINESTRING (0 0 100, 10 0.8 100, 20 .2 100, 30 0.8 100, 40 0.5"
                                        + " 100, 50 0.8 100)");
        line2.setUserData("TLM");

        final var graphs = LinesToRoutingGraphUtil.buildGraph(List.of(line1, line2), config);
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
                .map(e -> e.toString() + ": " + e.getProperties())
                .forEach(System.out::println);

        assertEquals(2, graphs.routingGraph().getVertices().size());
        assertEquals(2, graphs.routingGraph().getDirectedEdges().size());
        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream().allMatch(v -> v.degree() != 2));
    }

    @Test
    void testCrossingAtDifferentElevation() {
        final var wktReader = new WKTReader();

        final var lines =
                Stream.of("LINESTRING (0 0 0, 10 10 5)", "LINESTRING (10 0 10, 0 10 20)")
                        .map(ThrowingFunction.unchecked(wktReader::read))
                        .map(LineString.class::cast)
                        .map(line -> new LineWithInfo<>(line, null))
                        .toList();

        final var graph = LinesToGraphUtil.buildGraph(lines, LinesToGraphConfig.defaults());
        assertEquals(2, ConnectedComponents.computeComponents(graph).size());
    }

    @Test
    void testShortestRoutingEdge() throws ParseException {

        final var wktReader = new WKTReader();

        final var line1 =
                (LineString)
                        wktReader.read(
                                "LINESTRING (0 0 100, 10 -0.8 100, 20 .2 100, 30 0.8 100, 40 -0.5"
                                        + " 100, 50 0.8 100)");
        line1.setUserData("TLM");
        final var line2 = (LineString) wktReader.read("LINESTRING (0 0, 50 0)");
        line2.setUserData("SAC");

        final var graphs = LinesToRoutingGraphUtil.buildGraph(List.of(line1, line2), config);
        final var completeGraph = graphs.completeGraph();

        Assertions.assertNotNull(completeGraph);

        System.out.println(graphs.completeGraph());
        graphs.completeGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.completeGraph().getDirectedEdges().stream()
                .map(e -> e.toString() + ": " + e.getProperties())
                .forEach(System.out::println);

        System.out.println(graphs.routingGraph());
        graphs.routingGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.routingGraph().getDirectedEdges().stream()
                .map(e -> e.toString() + ": " + e.getProperties())
                .forEach(System.out::println);

        assertEquals(2, graphs.routingGraph().getVertices().size());
        assertEquals(2, graphs.routingGraph().getDirectedEdges().size());

        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream().allMatch(v -> v.degree() != 2));
    }

    @Test
    void testSacExample() throws ParseException {

        final var wktReader = new WKTReader();

        final var lineSac =
                (LineString)
                        wktReader.read(
                                "LINESTRING( 898.0 2973.0 , 894.0 2942.0 , 939.0 2836.0 , 915.0"
                                        + " 2780.0 , 860.0 2749.0 , 781.0 2735.0 )");
        lineSac.setUserData("SAC");
        final var lineTlm =
                (LineString)
                        wktReader.read(
                                "LINESTRING(896.0 2941.0 , 902.0 2926.0 , 916.0 2901.0 , 932.0"
                                        + " 2869.0 , 942.0 2835.0 , 941.0 2815.0 , 933.0 2797.0 , 918.0"
                                        + " 2779.0 , 891.0 2760.0 , 862.0 2748.0 , 826.0 2740.0 , 802.0"
                                        + " 2740.0 , 784.0 2734.0)");
        lineTlm.setUserData("TLM");

        final var graphs =
                LinesToRoutingGraphUtil.buildGraph(
                        List.of(lineTlm, lineSac),
                        LinesToGraphConfig.builder()
                                .snapTolerance(6.4)
                                .hasSnappingPriority("TLM"::equals)
                                .hasEdgePriority("SAC"::equals)
                                .build());

        final var completeGraph = graphs.completeGraph();

        Assertions.assertNotNull(completeGraph);

        System.out.println(graphs.completeGraph());
        graphs.completeGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.completeGraph().getDirectedEdges().stream()
                .map(e -> e.toString() + ": " + e.getProperties())
                .forEach(System.out::println);

        System.out.println(graphs.routingGraph());
        graphs.routingGraph().getVertices().stream()
                .map(v -> v.getId() + ": " + v.getProperties())
                .forEach(System.out::println);
        graphs.routingGraph().getDirectedEdges().stream()
                .map(e -> e.toString() + ": " + e.getProperties())
                .forEach(System.out::println);

        Assertions.assertTrue(
                graphs.routingGraph().getDirectedEdges().stream()
                        .allMatch(e -> e.getProperties().weight() > 0));

        Assertions.assertTrue(
                graphs.routingGraph().getVertices().stream()
                        .allMatch(
                                v ->
                                        graphs.completeGraph()
                                                        .getVertex(v.getId())
                                                        .orElseThrow()
                                                        .degree()
                                                != 2));

        GeoJsonUtil.saveGeoJson(GeoJsonUtil.routingGraphToFeatures(graphs).toList(), "sac-example");
    }
}
