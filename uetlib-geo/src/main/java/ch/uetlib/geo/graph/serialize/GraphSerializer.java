package ch.uetlib.geo.graph.serialize;

import static java.util.Objects.requireNonNullElseGet;

import ch.uetlib.core.utils.MapCollectors;
import ch.uetlib.geo.graph.models.Edge;
import ch.uetlib.geo.graph.models.Graph;
import ch.uetlib.geo.graph.models.Vertex;
import ch.uetlib.geo.jackson.JsonMapperJtsUtils;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public final class GraphSerializer<V, E> {

    private final Graph<V, E> graph;

    private final ObjectMapper objectMapper;

    public GraphSerializer(final Graph<V, E> graph) {
        this(graph, null);
    }

    public GraphSerializer(final Graph<V, E> graph, @Nullable final ObjectMapper objectMapper) {
        this.graph = graph;
        this.objectMapper =
                requireNonNullElseGet(objectMapper, JsonMapperJtsUtils::jsonMapperWgs84);
    }

    public void write(final File file) throws IOException {
        try (final var outputStream = new FileOutputStream(file)) {
            write(outputStream);
        }
    }

    public byte[] writeToBytes() throws IOException {
        try (final var outputStream = new ByteArrayOutputStream()) {
            write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void write(final OutputStream outputStream) throws IOException {
        final var serializableGraph = createSerializableGraph();
        objectMapper.writeValue(new GzipCompressorOutputStream(outputStream), serializableGraph);
    }

    private SerializableGraph<V, E> createSerializableGraph() {
        final var vertexProperties =
                graph.getVertices().stream()
                        .map(Vertex::getProperties)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        final var edgeProperties =
                graph.directedEdgesStream()
                        .map(Edge::getProperties)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        final var vertexPropertiesMap = indexMap(vertexProperties);
        final var edgePropertiesMap = indexMap(edgeProperties);
        return new SerializableGraph<>(
                graph.getGraphId(),
                graph.getVertices().stream()
                        .map(v -> toSerializable(v, vertexPropertiesMap))
                        .toList(),
                graph.getDirectedEdges().stream()
                        .map(e -> toSerializable(e, edgePropertiesMap))
                        .toList(),
                vertexProperties,
                edgeProperties);
    }

    private SerializableVertex toSerializable(
            final Vertex<V, E> vertex, final Map<V, Integer> indexMap) {
        return new SerializableVertex(
                vertex.getId(), Objects.requireNonNull(indexMap.get(vertex.getProperties())));
    }

    private SerializableEdge toSerializable(final Edge<V, E> edge, final Map<E, Integer> indexMap) {
        return new SerializableEdge(
                edge.getU().getId(),
                edge.getV().getId(),
                Objects.requireNonNull(indexMap.get(edge.getProperties())));
    }

    private static <E> Map<E, Integer> indexMap(final List<E> list) {
        return IntStream.range(0, list.size())
                .mapToObj(i -> Pair.of(list.get(i), i))
                .collect(MapCollectors.entriesToMap());
    }
}
