package ch.uetlib.geo.tile.vector.mapbox.filter.definition;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum FilterRuleType {
    INCLUDE,
    EXCLUDE;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
