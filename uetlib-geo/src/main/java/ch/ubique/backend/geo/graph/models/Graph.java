package ch.ubique.backend.geo.graph.models;

import java.util.*;
import java.util.function.Function;

/**
 * @param <V> type of the vertex properties
 * @param <E> type of the edge properties
 */
public class Graph<V, E> {
    private final Map<Long, Vertex<V, E> > vertices;
    private final Map<Long, Edge<V, E>> edges;

    private long lastEdgeId;

    public Graph() {
        vertices = new HashMap<>();
        edges = new HashMap<>();
        lastEdgeId = 0;
    }

    public Vertex<V, E> addVertex(Vertex<V, E>  vertex) {
        vertices.put(vertex.getId(), vertex);
        return vertex;
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


    public Vertex<V, E> getOrAddVertex(Vertex<V, E>  vertex) {
        if (!vertices.containsKey(vertex.getId())) {
            addVertex(vertex);
        }
        return vertices.get(vertex.getId());
    }

    public Vertex<V, E>  getVertex(long vertexId) {
        return vertices.get(vertexId);
    }

    public Edge<V, E>  getEdge(long edgeId) {
        return edges.get(edgeId);
    }

    public Edge<V, E> getOrAddEdge(long u, long v) {
        return getOrAddEdge(u, v, null);
    }

    public Edge<V, E> getOrAddEdge(Vertex<V, E>  u, Vertex<V, E>  v) {
        return getOrAddEdge(u, v, null);
    }

    public Edge<V, E> getOrAddEdge(long u, long v, E properties) {
        return getOrAddEdge(getOrAddVertex(u), getOrAddVertex(v), properties);
    }

    public Edge<V, E> getOrAddEdge(Edge<V, E>  edge) {
        return getOrAddEdge(edge.getU(), edge.getV(), edge.getProperties());
    }

    public Edge<V, E> getOrAddEdge(Vertex<V, E>  u, Vertex<V, E>  v, E properties) {
        final var existingEdge = getEdge(u, v);
        if (existingEdge.isPresent()) {
            return existingEdge.get();
        }

        final var edge = new Edge<>(lastEdgeId++, u, v, properties);

        edges.put(edge.getId(), edge);

        u.getModifiableEdges().add(edge);
        v.getModifiableEdges().add(edge);

        return edge;
    }

    public Optional<Edge<V, E>> getEdge(Edge<V, E> edge) {
        return getEdge(edge.getU(), edge.getV());
    }

    public Optional<Edge<V, E>> getEdge(Vertex<V, E> u, Vertex<V, E> v) {
        return getEdge(u.getId(), v.getId());
    }

    public Optional<Edge<V, E>> getEdge(long u, long v) {
        return getVertex(u).getEdgeTo(v);
    }

    public Collection<Vertex<V, E>> getVertices() {
        return vertices.values();
    }

    public Collection<Edge<V, E>> getEdges() {
        return edges.values();
    }

    @Override
    public String toString() {
        return String.format("Graph(n = %,d, m = %,d)'", vertices.size(), edges.size());
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
                final var next = current.getNeighborsStream().filter(v -> !v.equals(prev)).findAny();

                assert next.isPresent();

                previous = current;
                current = next.get();
            }
        }
        return cycles;
    }

    public <S, T> Graph<S, T> map(Function<Vertex<V, E>, Vertex<S, T>> vertexMapper, Function<Edge<V, E>, T> edgeMapper) {
        final var mappedGraph = new Graph<S, T>();
        getVertices().forEach(vertex -> mappedGraph.addVertex(vertexMapper.apply(vertex)));
        getEdges().forEach(edge -> mappedGraph.getOrAddEdge(edge.getU().getId(), edge.getV().getId(), edgeMapper.apply(edge)));
        return mappedGraph;
    }
}
