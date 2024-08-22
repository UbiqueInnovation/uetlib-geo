package ch.ubique.backend.geo.graph.serialize;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

record SerializableGraph<V, E>(
        long graphId,
        @NotNull Collection<SerializableVertex> vertices,
        @NotNull Collection<SerializableEdge> edges,
        @NotNull List<V> vertexProperties,
        @NotNull List<E> edgeProperties) {}
