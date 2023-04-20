package ch.ubique.backend.geo.graph.routing;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GraphType {
    COMPLETE,
    ROUTING,
    HIGHWAY;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
