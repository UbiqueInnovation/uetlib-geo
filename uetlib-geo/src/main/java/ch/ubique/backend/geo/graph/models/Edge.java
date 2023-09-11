package ch.ubique.backend.geo.graph.models;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public final class Edge<V, E> {

    private final long id;

    private final Vertex<V, E> u;
    private final Vertex<V, E> v;

    private E properties;

    public Edge(final long id, @NotNull final Vertex<V, E> u, @NotNull final Vertex<V, E> v) {
        this(id, u, v, null);
    }

    public Edge(
            final long id,
            @NotNull final Vertex<V, E> u,
            @NotNull final Vertex<V, E> v,
            final E properties) {
        this.id = id;
        this.u = u;
        this.v = v;
        this.properties = properties;
    }

    public Vertex<V, E> getU() {
        return u;
    }

    public Vertex<V, E> getFrom() {
        return u;
    }

    public Vertex<V, E> getV() {
        return v;
    }

    public Vertex<V, E> getOther(final long firstVertexId) {
        if (u.getId() == firstVertexId) {
            return v;
        }
        return u;
    }

    public long getId() {
        return id;
    }

    public Vertex<V, E> getOther(final Vertex<V, E> firstVertex) {
        if (firstVertex.equals(u)) {
            return v;
        }
        return u;
    }

    public Optional<Edge<V, E>> getTwin() {
        return getTo().getEdges().stream().filter(e -> u.equals(e.v) && v.equals(e.u)).findFirst();
    }

    public Vertex<V, E> getTo() {
        return v;
    }

    public boolean connects(final long vertexId1, final long vertexId2) {
        return (vertexId1 == u.getId() && vertexId2 == v.getId())
                || (vertexId1 == v.getId() && vertexId2 == u.getId());
    }

    public boolean connects(final Vertex<V, E> a, final Vertex<V, E> b) {
        return (a.equals(u) && b.equals(v)) || (a.equals(v) && b.equals(u));
    }

    public boolean hasCommonVertex(final Edge<V, E> other) {
        return containsVertex(other.u) || containsVertex(other.v);
    }

    public boolean containsVertex(final Vertex<V, E> vertex) {
        return vertex.equals(u) || vertex.equals(v);
    }

    public E getProperties() {
        return properties;
    }

    public Edge<V, E> setProperties(final E properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String toString() {
        return "{" + u + ", " + v + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final Edge<?, ?> edge)) {
            return false;
        }

        return new EqualsBuilder().append(id, edge.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
