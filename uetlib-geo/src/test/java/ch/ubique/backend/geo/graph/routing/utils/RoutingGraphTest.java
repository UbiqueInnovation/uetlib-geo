package ch.ubique.backend.geo.graph.routing.utils;

import static ch.ubique.backend.core.utils.InputStreamUtils.zipStreamForEntry;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.DurationUtils;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.algorithms.BasicAlgorithms;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.tlm.SwisstopoTlmParser;
import ch.ubique.backend.geo.tlm.TlmToGraphUtil;
import ch.ubique.backend.geo.utils.duration.HikeDuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;

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
                                (from, to, context) -> weight(from, to))
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
                                (from, to, context) -> weight(from, to))
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
                                (from, to, context) -> weight(from, to))
                        .verticalSplitThreshold(10)
                        .buildGraph();

        RoutingGraphTestUtils.dumpAsGeoJson(graphs);
        assertFalse(graphs.completeGraph().isEmpty());
        assertFalse(graphs.routingGraph().isEmpty());
        checkRoutingGraphsValid(graphs);
    }

    @Test
    @Disabled("Uses > 40GB of memory")
    void testSwitzerland() throws IOException {

        final var url =
                "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.xtf.zip";

        try (final var inputStream =
                zipStreamForEntry(new URL(url).openStream(), "TLM_STRASSEN.xtf")) {

            final var paths = SwisstopoTlmParser.parseData(inputStream);

            final var graphs =
                    TlmToGraphUtil.buildGraph(paths, (from, to, context) -> weight(from, to));

            RoutingGraphTestUtils.dumpAsGeoJson(graphs);
            assertFalse(graphs.completeGraph().isEmpty());
            assertFalse(graphs.routingGraph().isEmpty());
            checkRoutingGraphsValid(graphs);
        }
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
                    "direction vertex "
                            + edge.getProperties().direction()
                            + " not present in complete graph");
        }

        assertEquals(
                BasicAlgorithms.connectedComponents(graphs.completeGraph()).size(),
                BasicAlgorithms.connectedComponents(graphs.routingGraph()).size());
        checkTwins(graphs.completeGraph());
        checkTwins(graphs.routingGraph());
        checkDegrees(graphs.completeGraph());
        checkDegrees(graphs.routingGraph());
    }

    private static void checkDegrees(final Graph<?, ?> graph) {
        assertEquals(
                graph.getDirectedEdges().size(),
                graph.getVertices().stream().mapToInt(Vertex::degree).sum());
    }

    private static void checkTwins(final Graph<?, ?> graph) {
        for (final var edge : graph.getDirectedEdges()) {
            assertTrue(edge.getTwin().isPresent());
            assertEquals(edge, edge.getTwin().flatMap(Edge::getTwin).orElseThrow());
        }
    }

    private static float weight(final Coordinate from, final Coordinate to) {
        final var duration =
                HikeDuration.calculateDuration(from.distance(to), to.getZ() - from.getZ());
        return (float) DurationUtils.toFractionalSeconds(duration);
    }
}
