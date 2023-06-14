package ch.ubique.backend.geo.graph.routing.utils;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.algorithms.BasicAlgorithms;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

class RoutingGraphTest {

    private static final GeometryFactory geometryFactory = JtsConverter.LV95.geometryFactory();
    private static final ObjectMapper objectMapper =
            JsonMapper.builder().addModule(new JtsModule(geometryFactory)).build();

    @Test
    void testMountainTunnel() throws IOException {

        final var featureCollection = readFile("mountain_tunnel.geojson");

        final var graphs =
                new LinesToRoutingGraphUtil<>(
                                featureCollection.getFeatures().stream()
                                        .map(feature -> (LineString) feature.getGeometry())
                                        .toList(),
                                (from, to, context) -> (float) from.distance(to))
                        .buildGraph();

        RoutingGraphTestUtils.dumpAsGeoJson(graphs);

        assertFalse(graphs.completeGraph().isEmpty());
        assertFalse(graphs.routingGraph().isEmpty());
        assertEquals(4, graphs.routingGraph().getVertices().size());
        assertEquals(4, graphs.routingGraph().getDirectedEdges().size());
        assertEquals(2, BasicAlgorithms.connectedComponents(graphs.completeGraph()).size());
        assertEquals(2, BasicAlgorithms.connectedComponents(graphs.routingGraph()).size());
        checkRoutingGraphsValid(graphs);
    }

    @Test
    void testMountainTunnelLargerArea() throws IOException {

        final var featureCollection = readFile("mountain_tunnel_larger_area.geojson");

        final var graphs =
                new LinesToRoutingGraphUtil<>(
                                featureCollection.getFeatures().stream()
                                        .map(feature -> (LineString) feature.getGeometry())
                                        .toList(),
                                (from, to, context) -> (float) from.distance(to))
                        .buildGraph();

        RoutingGraphTestUtils.dumpAsGeoJson(graphs);
        assertFalse(graphs.completeGraph().isEmpty());
        assertFalse(graphs.routingGraph().isEmpty());
        assertEquals(39, graphs.routingGraph().getVertices().size());
        assertEquals(86, graphs.routingGraph().getDirectedEdges().size());
        assertEquals(1, BasicAlgorithms.connectedComponents(graphs.completeGraph()).size());
        assertEquals(1, BasicAlgorithms.connectedComponents(graphs.routingGraph()).size());
        checkRoutingGraphsValid(graphs);
    }

    @Test
    void testLargeExample() throws IOException {

        final var featureCollection = readFile("large_example.geojson");

        final var graphs =
                new LinesToRoutingGraphUtil<>(
                                featureCollection.getFeatures().stream()
                                        .map(feature -> (LineString) feature.getGeometry())
                                        .toList(),
                                (from, to, context) -> (float) from.distance(to))
                        .verticalSplitThreshold(500)
                        .buildGraph();

        RoutingGraphTestUtils.dumpAsGeoJson(graphs);
        assertFalse(graphs.completeGraph().isEmpty());
        assertFalse(graphs.routingGraph().isEmpty());
        checkRoutingGraphsValid(graphs);
    }

    private static FeatureCollection readFile(final String path) throws IOException {

        try (final var inputStream = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(inputStream, FeatureCollection.class);
        }
    }

    private static void checkRoutingGraphsValid(final RoutingGraphs graphs) {
        for (final var edge : graphs.routingGraph().getDirectedEdges()) {
            assertTrue(
                    graphs.routingGraph().getVertex(edge.getFrom().getId()).isPresent(),
                    edge.getFrom() + " not present in routing graph");
            assertTrue(
                    graphs.routingGraph().getVertex(edge.getTo().getId()).isPresent(),
                    edge.getTo() + " not present in routing graph");
            assertTrue(
                    graphs.completeGraph().getVertex(edge.getFrom().getId()).isPresent(),
                    edge.getFrom() + " not present in complete graph");
            assertTrue(
                    graphs.completeGraph().getVertex(edge.getTo().getId()).isPresent(),
                    edge.getTo() + " not present in complete graph");
            assertTrue(
                    graphs.completeGraph()
                            .getVertex(edge.getProperties().direction().getId())
                            .isPresent(),
                    edge.getProperties().direction() + " not present in complete graph");
        }

        checkTwins(graphs.completeGraph());
        checkTwins(graphs.routingGraph());
    }

    private static void checkTwins(final Graph<?, ?> graph) {
        for (final var edge : graph.getDirectedEdges()) {
            assertTrue(edge.getTwin().isPresent());
            assertEquals(edge, edge.getTwin().flatMap(Edge::getTwin).orElseThrow());
        }
    }
}
