package ch.ubique.backend.geo.gdal.common;

public enum GdalCRS {
    EPSG_4326("EPSG:4326"),
    EPSG_3857("EPSG:3857"),
    EPSG_2056("EPSG:2056"),
    EPSG_21781("EPSG:21781");

    public static final GdalCRS WGS84 = EPSG_4326;
    public static final GdalCRS WEB_MERCATOR = EPSG_3857;
    public static final GdalCRS LV03 = EPSG_21781;
    public static final GdalCRS LV95 = EPSG_2056;

    private final String crsString;

    GdalCRS(final String crsString) {
        this.crsString = crsString;
    }

    public String getCRSString() {
        return crsString;
    }
}
