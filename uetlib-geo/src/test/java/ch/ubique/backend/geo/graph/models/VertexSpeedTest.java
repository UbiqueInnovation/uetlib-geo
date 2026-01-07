package ch.ubique.backend.geo.graph.models;

import static org.assertj.core.api.Assertions.assertThat;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.graph.utils.LinesGraphBuilder;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class VertexSpeedTest {

    private static final Logger logger = LoggerFactory.getLogger(VertexSpeedTest.class);

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/furka.geojson",
                "/alp_la_schera.geojson",
                "/large_example.geojson",
                "/mountain_tunnel.geojson",
                "/mountain_tunnel_larger_area.geojson"
            })
    void testSpeedEdgeTo(final String path) throws IOException {
        final var featureCollection = readFile(path);
        final var lines =
                featureCollection.getFeatures().stream()
                        .map(GenericFeature::getGeometry)
                        .map(LineString.class::cast);

        final var graph =
                new LinesGraphBuilder<>(2)
                        .addLinesWithoutInfo(lines)
                        .createVerticesForIntersections()
                        .build(CoordinateSystems.WGS84);
        assertThat(graph).isNotNull();
        assertThat(graph.getVertices()).isNotEmpty();

        final var neighborPairs =
                graph.getVertices().stream()
                        .parallel()
                        .limit(10_000_000)
                        .flatMap(
                                vertex ->
                                        vertex.getNeighbors().stream()
                                                .map(neighbor -> Pair.of(vertex, neighbor)))
                        .toList();

        logger.info("Neighbor pairs size: {}", neighborPairs.size());

        final var stopWatch = new StopWatch();
        stopWatch.start();
        for (final var pair : neighborPairs) {
            pair.getLeft().getEdgeTo(pair.getRight());
        }
        stopWatch.stop();
        logger.info("First took {}", stopWatch.formatTime());
        stopWatch.reset();
    }

    private FeatureCollection readFile(final String path) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream(path)) {
            return ObjectMapperJtsUtils.jsonMapperLv95()
                    .readValue(inputStream, FeatureCollection.class);
        }
    }
}
