package ch.uetlib.geo.mbtile.metadata.constants;

public enum MbtileFormat {
    PBF("pbf"),
    JPG("jpg"),
    PNG("png"),
    WEBP("webp");

    private final String value;

    MbtileFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
