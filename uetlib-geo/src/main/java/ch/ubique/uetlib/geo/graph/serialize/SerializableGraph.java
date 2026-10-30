package ch.ubique.uetlib.geo.graph.serialize;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;

record SerializableGraph<V extends @Nullable Object, E extends @Nullable Object>(
        long graphId,
        Collection<SerializableVertex> vertices,
        Collection<SerializableEdge> edges,
        List<V> vertexProperties,
        List<E> edgeProperties) {}
