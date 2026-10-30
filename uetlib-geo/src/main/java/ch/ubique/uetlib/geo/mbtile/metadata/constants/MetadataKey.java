package ch.ubique.uetlib.geo.mbtile.metadata.constants;

/**
 * according to mbtiles v1.3 specifications
 * (https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md#content)
 */
public enum MetadataKey {
    NAME("name", true),
    FORMAT("format", true),
    BOUNDS("bounds", false),
    CENTER("center", false),
    MINZOOM("minzoom", false),
    MAXZOOM("maxzoom", false),
    ATTRIBUTION("attribution", false),
    DESCRIPTION("description", false),
    TYPE("type", false),
    VERSION("version", false),
    JSON("json", false);

    private final String value;
    private final boolean required;

    MetadataKey(String value, boolean required) {
        this.value = value;
        this.required = required;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequired(MbtileFormat tileFormat) {
        if (this == JSON) {
            return MbtileFormat.PBF.equals(tileFormat);
        } else {
            return this.required;
        }
    }
}
