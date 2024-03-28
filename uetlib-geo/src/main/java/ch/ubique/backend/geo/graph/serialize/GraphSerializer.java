package ch.ubique.backend.geo.graph.serialize;

import static ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils.objectMapperWgs84;

import static java.util.Objects.requireNonNullElse;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Nullable;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;

public final class GraphSerializer<V, E> {

    private final Graph<V, E> graph;

    private final ObjectMapper objectMapper;

    public GraphSerializer(final Graph<V, E> graph) {
        this(graph, null);
    }

    public GraphSerializer(final Graph<V, E> graph, @Nullable final ObjectMapper objectMapper) {
        this.graph = graph;
        this.objectMapper = requireNonNullElse(objectMapper, objectMapperWgs84());
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
        return new SerializableGraph<>(
                graph.getVertices().stream().map(this::toSerializable).toList(),
                graph.getDirectedEdges().stream().map(this::toSerializable).toList());
    }

    private SerializableVertex<V> toSerializable(final Vertex<V, E> vertex) {
        return new SerializableVertex<>(vertex.getId(), vertex.getProperties());
    }

    private SerializableEdge<E> toSerializable(final Edge<V, E> edge) {
        return new SerializableEdge<>(
                edge.getU().getId(), edge.getV().getId(), edge.getProperties());
    }
}
