package ch.ubique.backend.geo.graph.serialize;

import jakarta.annotation.Nullable;

record SerializableVertex<T>(long id, @Nullable T properties) {}
