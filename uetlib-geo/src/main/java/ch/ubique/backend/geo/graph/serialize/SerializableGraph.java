package ch.ubique.backend.geo.graph.serialize;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

record SerializableGraph<V, E>(
        long graphId,
        @NonNull Collection<SerializableVertex> vertices,
        @NonNull Collection<SerializableEdge> edges,
        @NonNull List<V> vertexProperties,
        @NonNull List<E> edgeProperties) {}
