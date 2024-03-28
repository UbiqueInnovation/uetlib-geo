package ch.ubique.backend.geo.graph.serialize;

import org.jetbrains.annotations.Nullable;

record SerializableEdge<T>(long u, long v, @Nullable T properties) {}
