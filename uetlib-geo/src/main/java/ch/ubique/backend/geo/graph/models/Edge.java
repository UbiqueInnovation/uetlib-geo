package ch.ubique.backend.geo.graph.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public final class Edge<V, E> {

    private final long id;

    private final Vertex<V, E> u;
    private final Vertex<V, E> v;

    private E properties;

    public Edge(long id, @NotNull Vertex<V, E> u, @NotNull Vertex<V, E> v) {
        this(id, u, v, null);
    }

    public Edge(long id, @NotNull Vertex<V, E> u, @NotNull Vertex<V, E> v, E properties) {
        this.id = id;
        if (u.getId() < v.getId()) {
            this.u = u;
            this.v = v;
        } else {
            this.u = v;
            this.v = u;
        }
        this.properties = properties;
    }

    public Vertex<V, E> getU() {
        return u;
    }

    public Vertex<V, E> getV() {
        return v;
    }

    public long getId() {
        return id;
    }

    public Vertex<V, E> getOther(Vertex<V, E> firstVertex) {
        if (firstVertex.equals(u)) {
            return v;
        }
        return u;
    }

    public Vertex<V, E> getOther(final long firstVertexId) {
        if (u.getId() == firstVertexId) {
            return v;
        }
        return u;
    }

    public boolean connects(long vertexId1, long vertexId2) {
        return (vertexId1 == u.getId() && vertexId2 == v.getId()) || (vertexId1 == v.getId() && vertexId2 == u.getId());
    }

    public boolean connects(Vertex<V, E> a, Vertex<V, E> b) {
        return (a.equals(u) && b.equals(v)) || (a.equals(v) && b.equals(u));
    }

    public E getProperties() {
        return properties;
    }

    public Edge<V, E> setProperties(E properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String toString() {
        return "{" + u + ", " + v + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final var edge = (Edge<?, ?>) o;

        return new EqualsBuilder().append(u, edge.u).append(v, edge.v).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(u).append(v).toHashCode();
    }
}
