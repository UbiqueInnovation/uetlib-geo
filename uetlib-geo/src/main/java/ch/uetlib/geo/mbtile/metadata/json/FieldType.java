package ch.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {
    NUMBER("Number"),
    BOOLEAN("Boolean"),
    STRING("String"),
    VARYING("String");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
