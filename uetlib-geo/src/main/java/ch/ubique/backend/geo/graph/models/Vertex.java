package ch.ubique.backend.geo.graph.models;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public final class Vertex<V, E> {
    private final long id;

    private Set<Edge<V, E>> edges;

    private V properties;

    public Vertex(final long id) {
        this(id, null);
    }

    public Vertex(final long id, final V properties) {
        this.id = id;
        this.properties = properties;
        this.edges = Set.of();
    }

    public long getId() {
        return id;
    }

    /**
     * Returns an unmodifiable instance, to prevent getEdges().add() calls. To add edges,
     * {@linkplain Graph#getOrAddUndirectedEdge} respectively {@linkplain
     * Graph#getOrAddDirectedEdge} should be used.
     */
    public Set<Edge<V, E>> getEdges() {
        return edges;
    }

    void setEdges(final Set<Edge<V, E>> edges) {
        this.edges = Collections.unmodifiableSet(edges);
    }

    public Optional<Edge<V, E>> getEdgeTo(final long neighborId) {
        return edges.stream().filter(e -> e.connects(id, neighborId)).findFirst();
    }

    public Optional<Edge<V, E>> getEdgeTo(final Vertex<V, E> neighbor) {
        return edges.stream().filter(e -> e.connects(this, neighbor)).findFirst();
    }

    public List<Vertex<V, E>> getNeighbors() {
        return getNeighborsStream().toList();
    }

    public Stream<Vertex<V, E>> getNeighborsStream() {
        return edges.stream().map(e -> id == e.getU().id ? e.getV() : e.getU());
    }

    public V getProperties() {
        return properties;
    }

    public void setProperties(final V properties) {
        this.properties = properties;
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
    public boolean equals(final Object o) {
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
