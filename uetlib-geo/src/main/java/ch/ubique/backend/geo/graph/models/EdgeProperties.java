package ch.ubique.backend.geo.graph.models;

public class EdgeProperties {

    private Double distance;
    private Vertex direction;

    public Double getDistance() {
        return distance;
    }

    public EdgeProperties setDistance(Double distance) {
        this.distance = distance;
        return this;
    }

    public Vertex getDirection() {
        return direction;
    }

    public EdgeProperties setDirection(Vertex direction) {
        this.direction = direction;
        return this;
    }
}
