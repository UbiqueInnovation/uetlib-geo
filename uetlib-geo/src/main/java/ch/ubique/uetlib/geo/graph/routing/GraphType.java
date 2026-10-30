package ch.ubique.uetlib.geo.graph.routing;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GraphType {
    COMPLETE,
    ROUTING,
    HIGHWAY_COMPLETE,
    HIGHWAY_ROUTING;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
