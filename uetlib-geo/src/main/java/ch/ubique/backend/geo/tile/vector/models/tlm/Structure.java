package ch.ubique.backend.geo.tile.vector.models.tlm;

import org.jspecify.annotations.Nullable;

public enum Structure {
    BRIDGE,
    TUNNEL,
    UNDERPASS,
    UNDERPASS_WITH_STAIRS,
    ROCK_SHED,
    BRIDGE_WITH_ROCK_SHED,
    BRIDGE_WITH_COVER,
    BRIDGE_WITH_STAIRS,
    DAM,
    DAM_WEIR,
    FOOTBRIDGE,
    STAIRS,
    FORD;

    public static @Nullable Structure parse(final String value) {
        return switch (value.toLowerCase()) {
            case "keine", "in/auf gebaeude" -> null;
            case "bruecke" -> BRIDGE;
            case "tunnel" -> TUNNEL;
            case "unterfuehrung" -> UNDERPASS;
            case "unterfuehrung mit treppe" -> UNDERPASS_WITH_STAIRS;
            case "galerie" -> ROCK_SHED;
            case "bruecke mit galerie" -> BRIDGE_WITH_ROCK_SHED;
            case "gedeckte bruecke" -> BRIDGE_WITH_COVER;
            case "bruecke mit treppe" -> BRIDGE_WITH_COVER;
            case "staudamm" -> DAM;
            case "staumauer, wehr" -> DAM_WEIR;
            case "steg" -> FOOTBRIDGE;
            case "treppe" -> STAIRS;
            case "furt" -> FORD;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }
}
