package ch.ubique.backend.geo.graph.serialize;

import static java.util.Objects.requireNonNullElseGet;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.jackson.JsonMapperJtsUtils;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jspecify.annotations.NonNull;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public final class GraphDeserializer<V, E> {

    private final ObjectMapper objectMapper;

    private final Type vertexPropertyType;
    private final Type edgePropertyType;

    public GraphDeserializer(final Class<V> vertexPropertyClass, final Class<E> edgePropertyClass) {
        this(null, vertexPropertyClass, edgePropertyClass);
    }

    public GraphDeserializer(
            final ObjectMapper objectMapper,
            final Class<V> vertexPropertyClass,
            final Class<E> edgePropertyClass) {
        this.objectMapper =
                requireNonNullElseGet(objectMapper, JsonMapperJtsUtils::jsonMapperWgs84);
        this.vertexPropertyType = vertexPropertyClass;
        this.edgePropertyType = edgePropertyClass;
    }

    public Graph<V, E> read(final byte[] bytes) throws IOException {
        try (final var inputStream = new ByteArrayInputStream(bytes)) {
            return read(inputStream);
        }
    }

    public Graph<V, E> read(final InputStream inputStream) throws IOException {
        final var serializableGraph =
                objectMapper.readValue(new GzipCompressorInputStream(inputStream), typeReference());
        final var graph = new Graph<V, E>(serializableGraph.graphId());
        serializableGraph
                .vertices()
                .forEach(vertex -> addVertex(graph, vertex, serializableGraph.vertexProperties()));
        serializableGraph
                .edges()
                .forEach(edge -> addEdge(graph, edge, serializableGraph.edgeProperties()));
        return graph;
    }

    private TypeReference<SerializableGraph<V, E>> typeReference() {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return new ParameterizedType() {
                    @Override
                    public Type @NonNull [] getActualTypeArguments() {
                        return new Type[] {vertexPropertyType, edgePropertyType};
                    }

                    @Override
                    public @NonNull Type getRawType() {
                        return SerializableGraph.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return SerializableGraph.class.getGenericSuperclass();
                    }
                };
            }
        };
    }

    private void addVertex(
            final Graph<V, E> graph,
            final SerializableVertex vertex,
            @NonNull final List<V> properties) {
        graph.addVertex(
                vertex.id(),
                Optional.ofNullable(vertex.propertyKey()).map(properties::get).orElse(null));
    }

    private void addEdge(
            final Graph<V, E> graph,
            final SerializableEdge edge,
            @NonNull final List<E> properties) {
        graph.addDirectedEdge(
                graph.getVertex(edge.u()).orElseThrow(),
                graph.getVertex(edge.v()).orElseThrow(),
                Optional.ofNullable(edge.propertyKey()).map(properties::get).orElse(null));
    }
}
