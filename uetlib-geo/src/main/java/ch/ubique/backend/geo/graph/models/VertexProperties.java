package ch.ubique.backend.geo.graph.models;

import java.util.Map;

public class VertexProperties {

    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public VertexProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
