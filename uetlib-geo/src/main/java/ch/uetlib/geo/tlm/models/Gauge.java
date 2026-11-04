package ch.uetlib.geo.tlm.models;

public enum Gauge {
    STANDARD,
    /** <= 1m */
    NARROW,
    /** <= 75cm */
    SMALL,
    STANDARD_AND_NARROW;

    public static Gauge parse(final String value) {
        return switch (value) {
            case "Normalspur" -> STANDARD;
            case "Schmalspur" -> NARROW;
            case "Kleinbahn" -> SMALL;
            case "Schmalspur mit Normalspur" -> STANDARD_AND_NARROW;
            default -> throw new IllegalArgumentException("Unknown gauge: " + value);
        };
    }
}
