package ch.ubique.backend.geo.graph.serialize;

import org.jspecify.annotations.Nullable;

record SerializableEdge(int u, int v, @Nullable Integer propertyKey) {}
