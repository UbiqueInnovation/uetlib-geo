package ch.ubique.backend.geo.gdal.warp;

public enum GdalResamplingMethod {
    NEAREST("nearest"),
    BILINEAR("bilinear"),
    CUBIC("cubic"),
    CUBIC_SPLINE("cubicspline"),
    LANCZOS("lanczos"),
    AVERAGE("average"),
    ROOT_MEAN_SQUARE("rms"),
    MODE("mode"),
    MAX("max"),
    MIN("min"),
    MED("med"),
    Q1("q1"),
    Q3("q3"),
    SUM("sum");

    private final String value;

    GdalResamplingMethod(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
