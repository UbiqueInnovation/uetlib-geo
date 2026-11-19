package ch.uetlib.geo.graph.routing;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum GraphType {
    COMPLETE,
    ROUTING,
    HIGHWAY_COMPLETE,
    HIGHWAY_ROUTING;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
