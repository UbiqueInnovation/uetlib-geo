package ch.ubique.backend.geo.graph.serialize;

import jakarta.annotation.Nullable;

record SerializableVertex(int id, @Nullable Integer propertyKey) {}
