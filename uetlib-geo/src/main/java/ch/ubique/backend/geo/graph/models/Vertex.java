package ch.ubique.backend.geo.graph.models;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Vertex {

    private final long id;
    private WGS84Coordinate coordinate;

    private final Set<Edge> edges;

    private VertexProperties properties;

    public Vertex(long id) {
        this.id = id;
        edges = new HashSet<>(2);
    }

    public long getId() {
        return id;
    }

    public Vertex setCoordinate(WGS84Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public WGS84Coordinate getCoordinate() {
        return coordinate;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public List<Vertex> getNeighbors() {
        return edges.stream()
                .map(e -> this.equals(e.getU()) ? e.getV() : e.getU())
                .collect(Collectors.toList());
    }

    public VertexProperties getProperties() {
        return properties;
    }

    public Vertex setProperties(VertexProperties properties) {
        this.properties = properties;
        return this;
    }

    public int degree() {
        return edges.size();
    }

    public double distanceTo(Vertex other) {
        return coordinate.distanceTo(other.coordinate);
    }

    public Vertex copyWithoutEdges() {
        Vertex copy = new Vertex(id);
        copy.coordinate = coordinate;
        return copy;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vertex vertex = (Vertex) o;

        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
