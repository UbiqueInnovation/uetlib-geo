package ch.ubique.backend.geo.graph.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
    private final Map<Long, Vertex> vertices;
    private final Set<Edge> edges;

    private long lastEdgeId;

    public Graph() {
        vertices = new HashMap<>();
        edges = new HashSet<>();
        lastEdgeId = 0;
    }

    public Vertex addVertex(long id) {
        Vertex v = new Vertex(id);
        return addVertex(v);
    }

    public Vertex addVertex(Vertex vertex) {
        if (!vertices.containsKey(vertex.getId())) {
            vertices.put(vertex.getId(), vertex);
        }
        return vertex;
    }

    public Vertex getVertex(long vertexId) {
        return vertices.computeIfAbsent(vertexId, Vertex::new);
    }

    public Edge addEdge(long u, long v) {
        return addEdge(u, v, null);
    }

    public Edge addEdge(Vertex u, Vertex v) {
        return addEdge(u, v, null);
    }

    public Edge addEdge(long u, long v, EdgeProperties properties) {
        return addEdge(getVertex(u), getVertex(v), properties);
    }

    public Edge addEdge(Vertex u, Vertex v, EdgeProperties properties) {
        Edge edge = new Edge(lastEdgeId++, u, v);
        edge.setProperties(properties);

        edges.add(edge);

        u.getEdges().add(edge);
        v.getEdges().add(edge);

        return edge;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return String.format("Graph(n = %,d, m = %,d)'", vertices.size(), edges.size());
    }
}
