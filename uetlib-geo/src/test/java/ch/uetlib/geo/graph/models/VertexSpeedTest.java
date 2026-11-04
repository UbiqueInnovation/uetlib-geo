package ch.uetlib.geo.graph.models;

import static ch.uetlib.geo.jackson.JsonMapperJtsUtils.jsonMapperLv95;

import static org.assertj.core.api.Assertions.assertThat;

import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.geojson.models.jts.FeatureCollection;
import ch.uetlib.geo.geojson.models.jts.GenericFeature;
import ch.uetlib.geo.graph.routing.models.LineWithInfo;
import ch.uetlib.geo.graph.utils.LinesGraphBuilder;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

class VertexSpeedTest {

    private static final Logger logger = LoggerFactory.getLogger(VertexSpeedTest.class);

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
    void testSpeedEdgeTo(final String path) throws IOException {
        final var featureCollection = readFile(path);
        final var lines =
                featureCollection.getFeatures().stream()
                        .map(GenericFeature::getGeometry)
                        .map(LineString.class::cast)
                        .filter(Objects::nonNull)
                        .map(line -> new LineWithInfo<@NonNull Object>(line, ""));

        final var graph =
                new LinesGraphBuilder<>(2)
                        .addLines(lines)
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
            return OBJECT_MAPPER.readValue(inputStream, FeatureCollection.class);
        }
    }
}
