package ch.ubique.backend.geo.graph.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.collections4.SetUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public class Graph<V, E> {
    private final Map<Long, Vertex<V, E>> vertices;
    private final Map<Long, Edge<V, E>> directedEdges;
    private final Map<Long, Set<Long>> outgoingEdgesIndex;
    private final Map<Long, Set<Long>> ingoingEdgesIndex;
    private long lastEdgeId;

    public Graph() {
        vertices = new HashMap<>();
        directedEdges = new HashMap<>();
        outgoingEdgesIndex = new HashMap<>();
        ingoingEdgesIndex = new HashMap<>();
        lastEdgeId = 0;
    }

    public Vertex<V, E> getOrAddVertex(final Vertex<V, E> vertex) {
        if (!vertices.containsKey(vertex.getId())) {
            addVertex(vertex);
        }
        return vertices.get(vertex.getId());
    }

    public Vertex<V, E> addVertex(final long vertexId, final V properties) {
        return addVertex(new Vertex<>(vertexId, properties));
    }

    public Vertex<V, E> addVertex(final Vertex<V, E> vertex) {
        vertices.put(vertex.getId(), vertex);
        return vertex;
    }

    public Vertex<V, E> createVertex(final V properties) {
        final var vertexId = vertices.isEmpty() ? 0 : Collections.max(vertices.keySet()) + 1;
        return addVertex(vertexId, properties);
    }

    public Vertex<V, E> getOrAddVertex(final long vertexId) {
        return getOrAddVertex(vertexId, null);
    }

    public Vertex<V, E> getOrAddVertex(final long vertexId, final V properties) {
        return getOrAddVertex(new Vertex<>(vertexId, properties));
    }

    public Optional<Edge<V, E>> getEdge(final long u, final long v) {
        return getVertex(u).flatMap(vertex -> vertex.getEdgeTo(v));
    }

    public Edge<V, E> getEdge(final long edgeId) {
        return directedEdges.get(edgeId);
    }

    public Edge<V, E> getOrAddUndirectedEdge(final long u, final long v) {
        return getOrAddUndirectedEdge(u, v, null);
    }

    public Edge<V, E> getOrAddUndirectedEdge(final Vertex<V, E> u, final Vertex<V, E> v) {
        return getOrAddUndirectedEdge(u, v, null);
    }

    public Edge<V, E> getOrAddUndirectedEdge(final long u, final long v, final E properties) {
        return getOrAddUndirectedEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    public Edge<V, E> getOrAddUndirectedEdge(
            final Vertex<V, E> a, final Vertex<V, E> b, final E properties) {
        getOrAddDirectedEdge(b, a, properties);
        return getOrAddDirectedEdge(a, b, properties);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(
            final Vertex<V, E> u, final Vertex<V, E> v, final E properties) {
        return getEdge(u, v).orElseGet(() -> addDirectedEdge(u, v, properties));
    }

    public Edge<V, E> addDirectedEdge(
            final Vertex<V, E> u, final Vertex<V, E> v, final E properties) {

        final var edge = new Edge<>(lastEdgeId, u, v, properties);
        lastEdgeId++;
        directedEdges.put(edge.getId(), edge);
        outgoingEdgesIndex.computeIfAbsent(u.getId(), key -> new HashSet<>()).add(edge.getId());
        ingoingEdgesIndex.computeIfAbsent(v.getId(), key -> new HashSet<>()).add(edge.getId());
        u.setEdges(SetUtils.union(u.getEdges(), Set.of(edge)).toSet());
        return edge;
    }

    public Edge<V, E> getOrAddUndirectedEdge(final Edge<V, E> edge) {
        return getOrAddUndirectedEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final long u, final long v) {
        return getOrAddDirectedEdge(u, v, null);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final long u, final long v, final E properties) {
        return getOrAddDirectedEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final Vertex<V, E> u, final Vertex<V, E> v) {
        return getOrAddDirectedEdge(u, v, null);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final Edge<V, E> edge) {
        return getOrAddDirectedEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    public Optional<Edge<V, E>> getEdge(final Edge<V, E> edge) {
        return getEdge(edge.getU(), edge.getV());
    }

    public Optional<Edge<V, E>> getEdge(final Vertex<V, E> u, final Vertex<V, E> v) {
        return getEdge(u.getId(), v.getId());
    }

    public Optional<Vertex<V, E>> getVertex(final long vertexId) {
        return Optional.ofNullable(vertices.get(vertexId));
    }

    public Collection<Vertex<V, E>> getVertices() {
        return vertices.values();
    }

    public Collection<Edge<V, E>> getDirectedEdges() {
        return directedEdges.values();
    }

    public void removeEdge(final long edgeId) {
        removeEdge(getEdge(edgeId));
    }

    public void removeEdge(final Edge<V, E> edge) {
        final var fromVertex = edge.getFrom();
        fromVertex.setEdges(SetUtils.difference(fromVertex.getEdges(), Set.of(edge)));
        directedEdges.remove(edge.getId());
        outgoingEdgesIndex.get(edge.getFrom().getId()).remove(edge.getId());
        ingoingEdgesIndex.get(edge.getTo().getId()).remove(edge.getId());
    }

    public void removeVertex(final Vertex<V, E> vertex) {
        outgoingEdgesIndex.getOrDefault(vertex.getId(), Set.of()).stream().toList().forEach(this::removeEdge);
        ingoingEdgesIndex.getOrDefault(vertex.getId(), Set.of()).stream().toList().forEach(this::removeEdge);
        vertices.remove(vertex.getId());
    }

    public void removeVertex(final long vertexId) {
        getVertex(vertexId).ifPresent(this::removeVertex);
    }

    @Override
    public String toString() {
        return String.format("Graph(n = %,d, m = %,d)", vertices.size(), directedEdges.size());
    }

    public Set<List<Vertex<V, E>>> findCycles() {

        final var visited = new HashSet<Vertex<V, E>>();
        final var cycles = new HashSet<List<Vertex<V, E>>>();

        for (final var start : getVertices()) {

            if (visited.contains(start) || start.degree() != 2) {
                continue;
            }

            Vertex<V, E> previous = null;
            var current = start;
            final var potentialCycle = new LinkedList<Vertex<V, E>>();
            while (current.degree() == 2) {

                if (current.equals(start) && !potentialCycle.isEmpty()) {
                    cycles.add(potentialCycle);
                    break;
                }

                visited.add(current);
                potentialCycle.add(current);

                final var prev = previous;
                final var next =
                        current.getNeighborsStream().filter(v -> !v.equals(prev)).findAny();

                assert next.isPresent();

                previous = current;
                current = next.get();
            }
        }
        return cycles;
    }

    /**
     * @param vertexPropertyMapper specifies how vertex properties are transformed.
     * @param edgePropertyMapper specifies how edge properties are transformed.
     * @return the mapped graph
     * @param <S> the vertex property type of the mapped graph
     * @param <T> the edge property type of the mapped graph
     */
    public <S, T> Graph<S, T> map(
            @NotNull final Function<Vertex<V, E>, S> vertexPropertyMapper,
            @NotNull final Function<Edge<V, E>, T> edgePropertyMapper) {
        return filterAndMap(null, vertexPropertyMapper, null, edgePropertyMapper);
    }

    /**
     * @param vertexFilter vertices where this predicate evaluates to true are kept. If null, no
     *     filter is applied.
     * @param edgeFilter edges where this predicate evaluates to true are kept. If null, no filter
     *     is applied.
     * @return the filtered graph
     */
    public Graph<V, E> filter(
            @Nullable final Predicate<Vertex<V, E>> vertexFilter,
            @Nullable final Predicate<Edge<V, E>> edgeFilter) {
        return filterAndMap(vertexFilter, Vertex::getProperties, edgeFilter, Edge::getProperties);
    }

    /**
     * @param vertexFilter vertices where this predicate evaluates to true are kept. If null, no
     *     filter is applied.
     * @param vertexPropertyMapper specifies how vertex properties are transformed.
     * @param edgeFilter edges where this predicate evaluates to true are kept. If null, no filter
     *     is applied.
     * @param edgePropertyMapper specifies how edge properties are transformed.
     * @return the filtered and mapped graph
     * @param <S> the vertex property type of the mapped graph
     * @param <T> the edge property type of the mapped graph
     */
    public <S, T> Graph<S, T> filterAndMap(
            @Nullable final Predicate<Vertex<V, E>> vertexFilter,
            @NotNull final Function<Vertex<V, E>, S> vertexPropertyMapper,
            @Nullable final Predicate<Edge<V, E>> edgeFilter,
            @NotNull final Function<Edge<V, E>, T> edgePropertyMapper) {

        final var graph = new Graph<S, T>();

        getVertices().stream()
                .filter(Objects.requireNonNullElse(vertexFilter, v -> true))
                .forEach(
                        vertex ->
                                graph.addVertex(
                                        vertex.getId(), vertexPropertyMapper.apply(vertex)));

        getDirectedEdges().stream()
                .filter(Objects.requireNonNullElse(edgeFilter, e -> true))
                .forEach(
                        edge ->
                                graph.getOrAddDirectedEdge(
                                        edge.getU().getId(),
                                        edge.getV().getId(),
                                        edgePropertyMapper.apply(edge)));
        return graph;
    }

    public boolean isEmpty() {
        return vertices.isEmpty() && directedEdges.isEmpty();
    }
}
