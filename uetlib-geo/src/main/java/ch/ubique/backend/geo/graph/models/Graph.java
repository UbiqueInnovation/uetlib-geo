package ch.ubique.backend.geo.graph.models;

import ch.ubique.backend.core.utils.SetUtils;

import java.util.*;
import java.util.function.Function;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public class Graph<V, E> {
    private final Map<Long, Vertex<V, E>> vertices;
    private final Map<Long, Edge<V, E>> directedEdges;

    private long lastEdgeId;

    public Graph() {
        vertices = new HashMap<>();
        directedEdges = new HashMap<>();
        lastEdgeId = 0;
    }

    public Vertex<V, E> getOrAddVertex(Vertex<V, E> vertex) {
        if (!vertices.containsKey(vertex.getId())) {
            addVertex(vertex);
        }
        return vertices.get(vertex.getId());
    }

    public Vertex<V, E> addVertex(long vertexId, V properties) {
        return addVertex(new Vertex<>(vertexId, properties));
    }

    public Vertex<V, E> getOrAddVertex(long vertexId) {
        return getOrAddVertex(vertexId, null);
    }

    public Vertex<V, E> getOrAddVertex(long vertexId, V properties) {
        return getOrAddVertex(new Vertex<>(vertexId, properties));
    }

    public Vertex<V, E> addVertex(Vertex<V, E> vertex) {
        vertices.put(vertex.getId(), vertex);
        return vertex;
    }

    public Optional<Edge<V, E>> getEdge(long u, long v) {
        return getVertex(u).flatMap(vertex -> vertex.getEdgeTo(v));
    }

    public Edge<V, E> getEdge(long edgeId) {
        return directedEdges.get(edgeId);
    }

    public Edge<V, E> getOrAddUndirectedEdge(long u, long v) {
        return getOrAddUndirectedEdge(u, v, null);
    }

    public Edge<V, E> getOrAddUndirectedEdge(Vertex<V, E> u, Vertex<V, E> v) {
        return getOrAddUndirectedEdge(u, v, null);
    }

    public Edge<V, E> getOrAddUndirectedEdge(long u, long v, E properties) {
        return getOrAddUndirectedEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    public Edge<V, E> getOrAddUndirectedEdge(Vertex<V, E> a, Vertex<V, E> b, E properties) {
        getOrAddDirectedEdge(b, a, properties);
        return getOrAddDirectedEdge(a, b, properties);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(Vertex<V, E> u, Vertex<V, E> v, E properties) {
        final var existingEdge = getEdge(u, v);
        if (existingEdge.isPresent()) {
            return existingEdge.get();
        }

        final var edge = new Edge<>(lastEdgeId++, u, v, properties);
        directedEdges.put(edge.getId(), edge);
        u.setEdges(SetUtils.union(u.getEdges(), Set.of(edge)));

        return edge;
    }

    public Edge<V, E> getOrAddUndirectedEdge(Edge<V, E> edge) {
        return getOrAddUndirectedEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(long u, long v) {
        return getOrAddDirectedEdge(u, v, null);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(long u, long v, E properties) {
        return getOrAddDirectedEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(Vertex<V, E> u, Vertex<V, E> v) {
        return getOrAddDirectedEdge(u, v, null);
    }

    /** Gets or adds a <strong>directed</strong> edge */
    public Edge<V, E> getOrAddDirectedEdge(Edge<V, E> edge) {
        return getOrAddDirectedEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    public Optional<Edge<V, E>> getEdge(Edge<V, E> edge) {
        return getEdge(edge.getU(), edge.getV());
    }

    public Optional<Edge<V, E>> getEdge(Vertex<V, E> u, Vertex<V, E> v) {
        return getEdge(u.getId(), v.getId());
    }

    public Optional<Vertex<V, E>> getVertex(long vertexId) {
        return Optional.ofNullable(vertices.get(vertexId));
    }

    public Collection<Vertex<V, E>> getVertices() {
        return vertices.values();
    }

    public Collection<Edge<V, E>> getDirectedEdges() {
        return directedEdges.values();
    }

    public void removeEdge(final Edge<V, E> edge) {
        final var fromVertex = edge.getFrom();
        fromVertex.setEdges(SetUtils.exclude(fromVertex.getEdges(), edge));
        directedEdges.remove(edge.getId());
    }

    public void removeVertex(final Vertex<V, E> vertex) {
        vertices.remove(vertex.getId());
    }

    @Override
    public String toString() {
        return String.format("Graph(n = %,d, m = %,d)", vertices.size(), directedEdges.size());
    }

    public Set<List<Vertex<V, E>>> findCycles() {

        final var visited = new HashSet<Vertex<V, E>>();
        final var cycles = new HashSet<List<Vertex<V, E>>>();

        for (var start : getVertices()) {

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

    public <S, T> Graph<S, T> map(
            Function<Vertex<V, E>, Vertex<S, T>> vertexMapper, Function<Edge<V, E>, T> edgeMapper) {
        final var mappedGraph = new Graph<S, T>();
        getVertices().forEach(vertex -> mappedGraph.addVertex(vertexMapper.apply(vertex)));
        getDirectedEdges()
                .forEach(
                        edge ->
                                mappedGraph.getOrAddUndirectedEdge(
                                        edge.getU().getId(),
                                        edge.getV().getId(),
                                        edgeMapper.apply(edge)));
        return mappedGraph;
    }

    public boolean isEmpty() {
        return vertices.isEmpty() && directedEdges.isEmpty();
    }
}
