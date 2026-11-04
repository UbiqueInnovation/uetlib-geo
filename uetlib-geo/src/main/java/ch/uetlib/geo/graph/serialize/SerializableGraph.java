package ch.uetlib.geo.graph.serialize;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

record SerializableGraph<V extends @Nullable Object, E extends @Nullable Object>(
        long graphId,
        Collection<SerializableVertex> vertices,
        Collection<SerializableEdge> edges,
        List<V> vertexProperties,
        List<E> edgeProperties) {}
