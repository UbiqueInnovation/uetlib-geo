package ch.ubique.backend.geo.graph.serialize;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.utils.LinesGraphBuilder;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.util.Objects;

class GraphSerializerTest {

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
                        .map(LineString.class::cast);

        final var graph =
                new LinesGraphBuilder<>(2, 5)
                        .addLinesWithoutInfo(lines)
                        .createVerticesForIntersections()
                        .build(CoordinateSystems.WGS84);

        final var serialized = new GraphSerializer<>(graph).writeToBytes();
        final var deserializedGraph =
                new GraphDeserializer<>(WGS84Coordinate.class, Object.class).read(serialized);
        assertTrue(graphsEqual(graph, deserializedGraph));
    }

    private <V, E> boolean graphsEqual(final Graph<V, E> a, final Graph<V, E> b) {
        return a.getVertices().size() == b.getVertices().size()
                && a.getVertices().stream().allMatch(vertex -> equalVertexExists(vertex, b))
                && a.getGraphId() == b.getGraphId();
    }

    private static <V, E> boolean equalVertexExists(
            final Vertex<V, E> vertex, final Graph<?, ?> graph) {
        final var otherMaybe = graph.getVertex(vertex.getId());
        if (otherMaybe.isEmpty()) {
            return false;
        }
        final var other = otherMaybe.get();
        if (vertex.getId() != other.getId()
                || !Objects.equals(vertex.getProperties(), other.getProperties())) {
            return false;
        }
        return vertex.outgoingEdges().size() == other.outgoingEdges().size()
                && vertex.outgoingEdges().stream().allMatch(edge -> equalEdgeExists(edge, other));
    }

    private static <V, E> boolean equalEdgeExists(
            final Edge<V, E> edge, final Vertex<?, ?> otherVertex) {
        final var otherMaybe = otherVertex.getEdgeTo(edge.getTo().getId());
        if (otherMaybe.isEmpty()) {
            return false;
        }
        final var other = otherMaybe.get();

        return Objects.equals(other.getProperties(), edge.getProperties());
    }

    private FeatureCollection readFile(final String path) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream(path)) {
            return ObjectMapperJtsUtils.jsonMapperLv95()
                    .readValue(inputStream, FeatureCollection.class);
        }
    }
}
