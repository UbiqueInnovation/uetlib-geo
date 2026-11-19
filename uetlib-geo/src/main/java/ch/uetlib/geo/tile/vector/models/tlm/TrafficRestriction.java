package ch.uetlib.geo.tile.vector.models.tlm;

import java.util.Locale;

public enum TrafficRestriction {
    NONE,
    CLOSED,
    TRAFFIC_BAN,
    GENERAL_TRAFFIC_RESTRICTION,
    GESICHERTE_KLETTERPARTIE,
    PANZERPISTE,
    MILITARY_ROAD,
    TIME_BASED,
    TEST_TRACK,
    RACING_TRACK;

    public static TrafficRestriction parse(final String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "keine" -> NONE;
            case "gesperrt" -> CLOSED;
            case "allgemeines fahrverbot" -> TRAFFIC_BAN;
            case "allgemeine verkehrsbeschraenkung" -> GENERAL_TRAFFIC_RESTRICTION;
            case "gesicherte kletterpartie" -> GESICHERTE_KLETTERPARTIE;
            case "militaerstrasse" -> MILITARY_ROAD;
            case "panzerpiste" -> PANZERPISTE;
            case "zeitlich geregelt" -> TIME_BASED;
            case "teststrecke" -> TEST_TRACK;
            case "rennstrecke" -> RACING_TRACK;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }
}
