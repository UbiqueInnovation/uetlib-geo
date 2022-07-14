package ch.ubique.backend.geo.graph.models;

import javax.validation.constraints.NotNull;

public class Edge {

    private final long id;

    private final Vertex u;
    private final Vertex v;

    private EdgeProperties properties;

    public Edge(long id, @NotNull Vertex u, @NotNull Vertex v) {
        this.id = id;
        if (u.getId() < v.getId()) {
            this.u = u;
            this.v = v;
        } else {
            this.u = v;
            this.v = u;
        }
    }

    public Vertex getU() {
        return u;
    }

    public Vertex getV() {
        return v;
    }

    public Vertex getOther(Vertex firstVertex) {
        if (firstVertex.equals(u)) {
            return v;
        }
        return u;
    }

    public long getId() {
        return id;
    }

    public EdgeProperties getProperties() {
        return properties;
    }

    public Edge setProperties(EdgeProperties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String toString() {
        return "{" + u + ", " + v + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Edge edge = (Edge) o;

        if (!u.equals(edge.u)) {
            return false;
        }
        return v.equals(edge.v);
    }

    @Override
    public int hashCode() {
        int result = u.hashCode();
        result = 31 * result + v.hashCode();
        return result;
    }
}
