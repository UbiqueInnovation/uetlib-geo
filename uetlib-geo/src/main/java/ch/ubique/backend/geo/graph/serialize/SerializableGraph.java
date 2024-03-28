package ch.ubique.backend.geo.graph.serialize;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

record SerializableGraph<V, E>(
        @NotNull Collection<SerializableVertex<V>> vertices,
        @NotNull Collection<SerializableEdge<E>> edges) {}
