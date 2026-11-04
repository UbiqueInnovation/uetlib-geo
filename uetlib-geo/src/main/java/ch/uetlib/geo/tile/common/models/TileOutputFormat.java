package ch.uetlib.geo.tile.common.models;

public enum TileOutputFormat {
    JSON,
    GEO_JSON,
    PBF,
    PNG;

    public String fileEnding() {
        return switch (this) {
            case PBF -> "pbf";
            case PNG -> "png";
            case JSON, GEO_JSON -> "json";
        };
    }
}
