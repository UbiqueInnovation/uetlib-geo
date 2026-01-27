package ch.ubique.backend.geo.graph.utils;

import static ch.ubique.backend.geo.jackson.JsonMapperJtsUtils.jsonMapperLv95;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

class LinesGraphBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = jsonMapperLv95();

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/furka.geojson",
                "/alp_la_schera.geojson",
                "/large_example.geojson",
                "/mountain_tunnel.geojson",
                "/mountain_tunnel_larger_area.geojson"
            })
    void test(final String path) throws IOException {
        final var featureCollection = readFile(path);
        final var lines =
                featureCollection.getFeatures().stream()
                        .map(GenericFeature::getGeometry)
                        .map(LineString.class::cast)
                        .toList();
        final var builder = new LinesGraphBuilder<>(2);
        lines.forEach(builder::addLineWithoutInfo);
        final var graph = builder.createVerticesForIntersections().build(CoordinateSystems.WGS84);
        assertNotNull(graph);
        assertFalse(graph.isEmpty());
        assertTrue(graph.getDirectedEdges().size() > featureCollection.getFeatures().size());

        final var graphWithDuplicateAddedLines =
                new LinesGraphBuilder<>(2)
                        .addLinesWithoutInfo(lines)
                        .addLineWithoutInfo(lines.getFirst())
                        .addLine(lines.getLast(), null)
                        .createVerticesForIntersections()
                        .build(CoordinateSystems.WGS84);

        assertNotNull(graphWithDuplicateAddedLines);
        assertFalse(graphWithDuplicateAddedLines.isEmpty());

        assertEquals(graph.getVertices().size(), graphWithDuplicateAddedLines.getVertices().size());
        assertEquals(
                graph.getDirectedEdges().size(),
                graphWithDuplicateAddedLines.getDirectedEdges().size());
    }

    private FeatureCollection readFile(final String path) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream(path)) {
            return OBJECT_MAPPER.readValue(inputStream, FeatureCollection.class);
        }
    }
}
