package ch.ubique.backend.geo.mbtile.metadata.json;

public enum FieldType {
    NUMBER("Number"),
    BOOLEAN("Boolean"),
    STRING("String"),
    VARYING("String");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
