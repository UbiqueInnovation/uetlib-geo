package ch.ubique.backend.geo.graph.serialize;

import org.jspecify.annotations.Nullable;

record SerializableVertex(int id, @Nullable Integer propertyKey) {}
