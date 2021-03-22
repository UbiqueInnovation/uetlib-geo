package ch.ubique.backend.geo.graph.models;

import java.util.HashMap;
import java.util.Map;

public class EdgeProperties {

    private Double distance;
    private Vertex direction;

    private Map<String, String> tags;

    public EdgeProperties() {
        this.tags = new HashMap<>();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public EdgeProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

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
