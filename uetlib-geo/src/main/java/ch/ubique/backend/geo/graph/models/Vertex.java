package ch.ubique.backend.geo.graph.models;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public final class Vertex<V, E> {
    private final long id;

    private final Set<Edge<V, E>> edges;

    private V properties;

    public Vertex(long id) {
        this(id, null);
    }

    public Vertex(long id, V properties) {
        this.id = id;
        this.properties = properties;
        edges = new HashSet<>(2);
    }

    public long getId() {
        return id;
    }

    Set<Edge<V, E>> getModifiableEdges() {
        return edges;
    }

    /**
     * Returns an unmodifiable instance, to prevent getEdges().add() calls.
     * To add edges, {@linkplain Graph#getOrAddEdge} should be used.
     */
    public Set<Edge<V, E>> getEdges() {
        return edges.stream().collect(Collectors.toUnmodifiableSet());
    }

    public Optional<Edge<V, E>> getEdgeTo(long neighborId) {
        return edges.stream().filter(e -> e.connects(id, neighborId)).findFirst();
    }

    public List<Vertex<V, E>> getNeighbors() {
        return edges.stream().map(e -> this.equals(e.getU()) ? e.getV() : e.getU()).toList();
    }

    public void setProperties(V properties) {
        this.properties = properties;
    }

    public V getProperties() {
        return properties;
    }

    public int degree() {
        return edges.size();
    }

    public Vertex<V, E> copyWithoutEdges() {
        return new Vertex<>(id, properties);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final var vertex = (Vertex<?, ?>) o;

        return new EqualsBuilder().append(id, vertex.id).isEquals();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }


}
