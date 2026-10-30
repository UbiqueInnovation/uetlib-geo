package ch.ubique.uetlib.geo.graph.models;

import static ch.ubique.uetlib.core.utils.SmallCollectionUtil.combine;
import static ch.ubique.uetlib.core.utils.SmallCollectionUtil.compress;
import static java.util.function.Predicate.not;

import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jspecify.annotations.Nullable;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public final class Vertex<V extends @Nullable Object, E extends @Nullable Object> {
    private final int id;

    private Collection<Edge<V, E>> outgoingEdges;
    private Collection<Edge<V, E>> incomingEdges;

    private V properties;

    public Vertex(final int id, final V properties) {
        this.id = id;
        this.properties = properties;
        this.outgoingEdges = List.of();
        this.incomingEdges = List.of();
    }

    public int getId() {
        return id;
    }

    /**
     * Returns an unmodifiable instance, to prevent getEdges().add() calls. To add edges,
     * {@linkplain Graph#getOrAddUndirectedEdge} respectively {@linkplain
     * Graph#getOrAddDirectedEdge} should be used.
     */
    public Collection<Edge<V, E>> outgoingEdges() {
        return outgoingEdges;
    }

    public Collection<Edge<V, E>> incomingEdges() {
        return incomingEdges;
    }

    public void addOutgoingEdge(final Edge<V, E> edge) {
        this.outgoingEdges = combine(outgoingEdges, edge);
    }

    public void addIncomingEdge(final Edge<V, E> edge) {
        this.incomingEdges = combine(incomingEdges, edge);
    }

    public void removeOutgoingEdge(final Edge<V, E> edge) {
        this.outgoingEdges = compress(outgoingEdges.stream().filter(not(edge::equals)));
    }

    public void removeIncomingEdge(final Edge<V, E> edge) {
        this.incomingEdges = compress(incomingEdges.stream().filter(not(edge::equals)));
    }

    public Optional<Edge<V, E>> getEdgeTo(final long neighborId) {
        for (final var e : outgoingEdges) {
            if (e.getTo().id == neighborId) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    public Optional<Edge<V, E>> getEdgeTo(final Vertex<V, E> neighbor) {
        for (final var e : outgoingEdges) {
            if (e.getTo().equals(neighbor)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    public List<Vertex<V, E>> getNeighbors() {
        return getNeighborsStream().toList();
    }

    public Stream<Vertex<V, E>> getNeighborsStream() {
        return outgoingEdges.stream().map(Edge::getTo);
    }

    public int getNeighborsCount() {
        return (int) getNeighborsStream().distinct().count();
    }

    public V getProperties() {
        return properties;
    }

    public void setProperties(final V properties) {
        this.properties = properties;
    }

    public int degree() {
        return outgoingEdges.size();
    }

    public Vertex<V, E> copyWithoutEdges() {
        return new Vertex<>(id, properties);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final Vertex<?, ?> vertex)) {
            return false;
        }

        return new EqualsBuilder().append(id, vertex.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
