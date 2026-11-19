package ch.uetlib.geo.graph.models;

import static java.util.Objects.requireNonNullElse;

import org.apache.commons.lang3.function.Suppliers;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public class Graph<V extends @Nullable Object, E extends @Nullable Object> {
    public static final long DEFAULT_GRAPH_ID = 0;

    private final long graphId;
    private final Map<Integer, Vertex<V, E>> vertices;
    private final AtomicInteger maxVertexId;

    public Graph() {
        this(DEFAULT_GRAPH_ID);
    }

    public Graph(final long graphId) {
        this.graphId = graphId;
        vertices = new HashMap<>();
        maxVertexId = new AtomicInteger();
    }

    public long getGraphId() {
        return graphId;
    }

    public Vertex<V, E> getOrAddVertex(final Vertex<V, E> vertex) {
        final var existingVertex = vertices.get(vertex.getId());
        if (existingVertex == null) {
            return addVertex(vertex);
        }
        return existingVertex;
    }

    public Vertex<V, E> addVertex(final int vertexId, final V properties) {
        return addVertex(new Vertex<>(vertexId, properties));
    }

    public Vertex<V, E> addVertex(final Vertex<V, E> vertex) {
        final var vertexId = vertex.getId();
        maxVertexId.set(Math.max(vertexId, maxVertexId.get()));
        vertices.put(vertexId, vertex);
        return vertex;
    }

    public Vertex<V, E> createVertex(final V properties) {
        final var vertexId = maxVertexId.incrementAndGet();
        return addVertex(vertexId, properties);
    }

    public Vertex<V, E> getOrAddVertex(final int vertexId) {
        return getOrAddVertex(vertexId, id -> null);
    }

    public Vertex<V, E> getOrAddVertex(final int vertexId, final V properties) {
        return getOrAddVertex(vertexId, id -> properties);
    }

    public Vertex<V, E> getOrAddVertex(
            final int vertexId, final IntFunction<@Nullable V> properties) {
        return vertices.computeIfAbsent(vertexId, id -> new Vertex<>(id, properties.apply(id)));
    }

    public Optional<Edge<V, E>> getEdge(final int u, final int v) {
        return getVertex(u).flatMap(vertex -> vertex.getEdgeTo(v));
    }

    public Edge<V, E> getOrAddUndirectedEdge(final int u, final int v, final E properties) {
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

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(
            final Vertex<V, E> u, final Vertex<V, E> v, final Supplier<E> properties) {
        return getEdge(u, v).orElseGet(() -> addDirectedEdge(u, v, properties.get()));
    }

    public Edge<V, E> addDirectedEdge(
            final Vertex<V, E> u, final Vertex<V, E> v, final E properties) {
        final var edge = new Edge<>(u, v, properties);
        u.addOutgoingEdge(edge);
        v.addIncomingEdge(edge);
        return edge;
    }

    public Edge<V, E> getOrAddUndirectedEdge(final Edge<V, E> edge) {
        return getOrAddUndirectedEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final int u, final int v, final E properties) {
        return getOrAddDirectedEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(final Vertex<V, E> u, final Vertex<V, E> v) {
        return getOrAddDirectedEdge(u, v, Suppliers.nul());
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

    public Optional<Vertex<V, E>> getVertex(final int vertexId) {
        return Optional.ofNullable(vertices.get(vertexId));
    }

    public Collection<Vertex<V, E>> getVertices() {
        return vertices.values();
    }

    public Collection<Edge<V, E>> getDirectedEdges() {
        return directedEdgesStream().toList();
    }

    public Stream<Edge<V, E>> directedEdgesStream() {
        return vertices.values().stream().map(Vertex::outgoingEdges).flatMap(Collection::stream);
    }

    public void removeEdge(final Edge<V, E> edge) {
        edge.getFrom().removeOutgoingEdge(edge);
        edge.getTo().removeIncomingEdge(edge);
    }

    public void removeVertex(final Vertex<V, E> vertex) {
        vertex.outgoingEdges().stream().toList().forEach(this::removeEdge);
        vertex.incomingEdges().stream().toList().forEach(this::removeEdge);
        vertices.remove(vertex.getId());
    }

    public void removeVertex(final int vertexId) {
        getVertex(vertexId).ifPresent(this::removeVertex);
    }

    @Override
    public String toString() {
        return String.format("Graph[n=%,d]", vertices.size());
    }

    public Set<List<Vertex<V, E>>> findCycles() {

        final var visited = new HashSet<Vertex<V, E>>();
        final var cycles = new HashSet<List<Vertex<V, E>>>();

        for (final var start : getVertices()) {

            if (visited.contains(start) || start.getNeighborsCount() != 2) {
                continue;
            }

            Vertex<V, E> previous = null;
            var current = start;
            final var potentialCycle = new ArrayList<Vertex<V, E>>();
            while (current.getNeighborsCount() == 2) {

                if (current.equals(start) && !potentialCycle.isEmpty()) {
                    cycles.add(potentialCycle);
                    break;
                }

                visited.add(current);
                potentialCycle.add(current);

                final var prev = previous;
                final var next =
                        current.getNeighborsStream().filter(v -> !v.equals(prev)).findAny();

                previous = current;
                current = next.orElseThrow();
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
            final Function<Vertex<V, E>, S> vertexPropertyMapper,
            final Function<Edge<V, E>, T> edgePropertyMapper) {
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
     * @param nullableVertexFilter vertices where this predicate evaluates to true are kept. If
     *     null, no filter is applied.
     * @param vertexPropertyMapper specifies how vertex properties are transformed.
     * @param nullableEdgeFilter edges where this predicate evaluates to true are kept. If null, no
     *     filter is applied.
     * @param edgePropertyMapper specifies how edge properties are transformed.
     * @param <S> the vertex property type of the mapped graph
     * @param <T> the edge property type of the mapped graph
     * @return A new Graph (filtered and mapped) with the same {@link #graphId} as the old graph
     * @see #filterAndMap(LongUnaryOperator, Predicate, Function, Predicate, Function)
     */
    public <S extends @Nullable Object, T extends @Nullable Object> Graph<S, T> filterAndMap(
            @Nullable final Predicate<Vertex<V, E>> nullableVertexFilter,
            final Function<Vertex<V, E>, S> vertexPropertyMapper,
            @Nullable final Predicate<Edge<V, E>> nullableEdgeFilter,
            final Function<Edge<V, E>, T> edgePropertyMapper) {
        return filterAndMap(
                i -> i,
                nullableVertexFilter,
                vertexPropertyMapper,
                nullableEdgeFilter,
                edgePropertyMapper);
    }

    /**
     * @param idMapper function to generate a new id
     * @param nullableVertexFilter vertices where this predicate evaluates to true are kept. If
     *     null, no filter is applied.
     * @param vertexPropertyMapper specifies how vertex properties are transformed.
     * @param nullableEdgeFilter edges where this predicate evaluates to true are kept. If null, no
     *     filter is applied.
     * @param edgePropertyMapper specifies how edge properties are transformed.
     * @param <S> the vertex property type of the mapped graph
     * @param <T> the edge property type of the mapped graph
     * @return A new Graph (filtered and mapped) with the same {@link #graphId} as the old graph
     * @see #filterAndMap(Predicate, Function, Predicate, Function)
     */
    public <S extends @Nullable Object, T extends @Nullable Object> Graph<S, T> filterAndMap(
            final LongUnaryOperator idMapper,
            @Nullable final Predicate<Vertex<V, E>> nullableVertexFilter,
            final Function<Vertex<V, E>, S> vertexPropertyMapper,
            @Nullable final Predicate<Edge<V, E>> nullableEdgeFilter,
            final Function<Edge<V, E>, T> edgePropertyMapper) {

        final var graph = new Graph<S, T>(idMapper.applyAsLong(graphId));

        final var vertexFilter = requireNonNullElse(nullableVertexFilter, v -> true);
        final var edgeFilter =
                requireNonNullElse(nullableEdgeFilter, e -> true)
                        .and(e -> vertexFilter.test(e.getU()))
                        .and(e -> vertexFilter.test(e.getV()));

        getVertices().stream()
                .filter(vertexFilter)
                .forEach(
                        vertex ->
                                graph.addVertex(
                                        vertex.getId(), vertexPropertyMapper.apply(vertex)));

        getDirectedEdges().stream()
                .filter(edgeFilter)
                .forEach(
                        edge ->
                                graph.addDirectedEdge(
                                        graph.getVertex(edge.getU().getId()).orElseThrow(),
                                        graph.getVertex(edge.getV().getId()).orElseThrow(),
                                        edgePropertyMapper.apply(edge)));
        return graph;
    }

    public boolean isEmpty() {
        return vertices.isEmpty();
    }
}
