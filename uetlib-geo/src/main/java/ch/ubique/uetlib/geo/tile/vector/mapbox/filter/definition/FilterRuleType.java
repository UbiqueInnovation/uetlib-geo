package ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FilterRuleType {
    INCLUDE,
    EXCLUDE;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }
}
