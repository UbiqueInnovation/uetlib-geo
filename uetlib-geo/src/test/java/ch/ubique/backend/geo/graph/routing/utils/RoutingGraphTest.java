package ch.ubique.backend.geo.graph.routing.utils;

import static ch.ubique.backend.core.utils.InputStreamUtils.zipStreamForEntry;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.DurationUtils;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.graph.algorithms.ConnectedComponents;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.tlm.SwisstopoTlmParser;
import ch.ubique.backend.geo.tlm.TlmToGraphUtil;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.WegType;
import ch.ubique.backend.geo.utils.duration.HikeDuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

class RoutingGraphTest {

    private static final GeometryFactory geometryFactory = CoordinateSystems.LV95.geometryFactory();
    private static final ObjectMapper objectMapper =
            JsonMapper.builder().addModule(new JtsModule(geometryFactory)).build();

    private static FeatureCollection readFile(final String path) throws IOException {

        try (final var inputStream = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(inputStream, FeatureCollection.class);
        }
    }

    private static void checkRoutingGraphsValid(final RoutingGraphs graphs) throws IOException {
        RoutingGraphTestUtils.dumpAsGeoJson(graphs);

        assertFalse(graphs.completeGraph().isEmpty());
        assertFalse(graphs.routingGraph().isEmpty());

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

        checkTwins(graphs.completeGraph());
        checkTwins(graphs.routingGraph());
        checkDegrees(graphs.completeGraph());
        checkDegrees(graphs.routingGraph());
        assertTrue(
                ConnectedComponents.computeComponents(graphs.completeGraph()).size()
                        >= ConnectedComponents.computeComponents(graphs.routingGraph()).size());
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

    private static RoutingGraphs createGraphsFromGeoJson(final String path) throws IOException {
        final var featureCollection = readFile(path);

        return LinesToRoutingGraphUtil.buildGraph(
                featureCollection.getFeatures().stream()
                        .map(feature -> (LineString) feature.getGeometry())
                        .toList(),
                LinesToGraphConfig.defaults());
    }

    @Test
    void testMountainTunnel() throws IOException {
        final var graphs = createGraphsFromGeoJson("mountain_tunnel.geojson");

        checkRoutingGraphsValid(graphs);
        assertEquals(4, graphs.routingGraph().getVertices().size());
        assertEquals(4, graphs.routingGraph().getDirectedEdges().size());
        assertEquals(2, ConnectedComponents.computeComponents(graphs.completeGraph()).size());
        assertEquals(2, ConnectedComponents.computeComponents(graphs.routingGraph()).size());
    }

    @Test
    void testMountainTunnelLargerArea() throws IOException {
        final var graphs = createGraphsFromGeoJson("mountain_tunnel_larger_area.geojson");

        checkRoutingGraphsValid(graphs);
        assertEquals(39, graphs.routingGraph().getVertices().size());
        assertEquals(86, graphs.routingGraph().getDirectedEdges().size());
        assertEquals(1, ConnectedComponents.computeComponents(graphs.completeGraph()).size());
        assertEquals(1, ConnectedComponents.computeComponents(graphs.routingGraph()).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"furka", "alp_la_schera", "large_example"})
    void testFurka(final String test) throws IOException {
        final var graphs = createGraphsFromGeoJson(test + ".geojson");
        checkRoutingGraphsValid(graphs);
    }

    @Test
    @Disabled("Uses > 40GB of memory")
    void testSwitzerland() throws IOException {

        final var url =
                "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.gpkg.zip";

        try (final var inputStream =
                new ZipArchiveInputStream(URI.create(url).toURL().openStream())) {
            inputStream.getNextEntry();

            final var paths = SwisstopoTlmParser.parsePaths(inputStream);

            final var graphs =
                    TlmToGraphUtil.buildGraph(paths, (from, to, context) -> weight(from, to));

            checkRoutingGraphsValid(graphs);
        }
    }

    @Test
    @Disabled(
            "Not an actual test. This is just a helper for creating extracts to use in other"
                    + " tests.")
    void extractTlmRegionToGeoJson() throws IOException {
        final var url =
                "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.xtf.zip";

        try (final var inputStream =
                zipStreamForEntry(URI.create(url).toURL().openStream(), "TLM_STRASSEN.xtf")) {

            final var excludedTypes =
                    Set.of(
                            WegType.AUSFAHRT,
                            WegType.EINFAHRT,
                            WegType.FAEHRE,
                            WegType.AUTOBAHN,
                            WegType.AUTOSTRASSE,
                            WegType.AUTOZUG);

            final var bounds =
                    new Envelope(
                            new LV95Coordinate(2_809_230, 1_170_855).toJts(),
                            new LV95Coordinate(2_812_120, 1_167_265).toJts());

            final var selectedPaths =
                    SwisstopoTlmParser.parsePaths(inputStream).stream()
                            .filter(path -> !excludedTypes.contains(path.info().type()))
                            .map(TlmPath::line)
                            .filter(geometry -> bounds.intersects(geometry.getEnvelopeInternal()))
                            .toList();

            RoutingGraphTestUtils.dumpAsGeoJson(selectedPaths);
        }
        fail("Please disable this test, it's just a helper to generated geojson extracts.");
    }
}
