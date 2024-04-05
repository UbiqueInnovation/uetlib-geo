package ch.ubique.backend.geo.tile.vector.models.tlm;

public enum TlmSource {
    SWISSTOPO,
    TRANSPORTATION_COMPANY;

    public static TlmSource parse(final String value) {
        return switch (value) {
            case "swisstopo" -> SWISSTOPO;
            case "Transportunternehmen" -> TRANSPORTATION_COMPANY;
            default -> throw new IllegalArgumentException("Unknown TlmSource: " + value);
        };
    }
}
