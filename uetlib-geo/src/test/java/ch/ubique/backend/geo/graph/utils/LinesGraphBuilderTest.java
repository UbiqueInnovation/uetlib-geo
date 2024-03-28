package ch.ubique.backend.geo.graph.utils;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

class LinesGraphBuilderTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "furka.geojson",
                "alp_la_schera.geojson",
                "large_example.geojson",
                "mountain_tunnel.geojson",
                "mountain_tunnel_larger_area.geojson"
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

    private static FeatureCollection readFile(final String path) throws IOException {

        try (final var inputStream = new ClassPathResource(path).getInputStream()) {
            return ObjectMapperJtsUtils.objectMapperLv95()
                    .readValue(inputStream, FeatureCollection.class);
        }
    }
}
