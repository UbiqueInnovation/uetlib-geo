package ch.uetlib.geo.tlm.models;

public enum RailServiceType {
    TRAIN,
    METRO,
    TRAM;

    public static RailServiceType parse(final String value) {
        return switch (value) {
            case "Bahn" -> TRAIN;
            case "Metro" -> METRO;
            case "Tram" -> TRAM;
            default -> throw new IllegalArgumentException("Unknown rail service type: " + value);
        };
    }
}
