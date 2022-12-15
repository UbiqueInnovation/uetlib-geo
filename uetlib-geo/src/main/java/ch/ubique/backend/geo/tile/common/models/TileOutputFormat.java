package ch.ubique.backend.geo.tile.common.models;

public enum TileOutputFormat {
    GEO_JSON,
    PBF,
    PNG;

    public String fileEnding() {
        return switch (this) {
            case PBF -> "pbf";
            case PNG -> "png";
            case GEO_JSON -> "json";
        };
    }
}
