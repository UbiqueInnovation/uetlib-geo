package ch.ubique.backend.geo.gdal.common;

public record Proj4Declaration(
        FileConverterProjection projection,
        float lat0,
        float lon0,
        float lat_ts,
        int a,
        int b,
        ProjectionUnit units) {
    public String toProj4String() {
        return String.format(
                "+proj=%s +lat_0=%f +lon_0=%f +lat_ts=%f +a=%d +b=%d +units=%s",
                projection, lat0, lon0, lat_ts, a, b, units.shortName);
    }

    public enum ProjectionUnit {
        METERS("m");

        private final String shortName;

        ProjectionUnit(final String unit) {
            this.shortName = unit;
        }
    }

    public enum FileConverterProjection {
        MERCATOR("merc"),
        TRANSVERSE_MERCATOR("tmerc"),
        LAMBERT_CONFORMAL_CONIC("lcc"),
        ORTHOGRAPHIC("ortho"),
        STEREOGRAPHIC("stere"),
        AZIMUTHAL_EQUIDISTANT("aeqd"),
        EQUIDISTANT_CYLINDRICAL("eqc"),
        GNOMONIC("gnom"),
        ALBERS_EQUAL_AREA_CONIC("aea"),
        SINUSOIDAL("sinu"),
        EQUIRECTANGULAR("eqc"),
        LAMBERT_AZIMUTHAL_EQUAL_AREA("laea"),
        LAMBERT_STEREOGRAPHIC("sterea"),
        OBLIQUE_MERCATOR("omerc");

        private final String projection;

        FileConverterProjection(final String projection) {
            this.projection = projection;
        }
    }
}
