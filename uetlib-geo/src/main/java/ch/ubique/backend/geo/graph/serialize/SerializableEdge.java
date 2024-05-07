package ch.ubique.backend.geo.graph.serialize;

import org.jetbrains.annotations.Nullable;

record SerializableEdge(int u, int v, @Nullable Integer propertyKey) {}
