package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import javax.validation.constraints.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class JtsConverter {
    private static final GeometryFactory GEOMETRY_FACTORY_WGS84 =
            new GeometryFactory(new PrecisionModel(), 4326);
    private static final GeometryFactory GEOMETRY_FACTORY_LV03 =
            new GeometryFactory(new PrecisionModel(), 21781);
    private static final GeometryFactory GEOMETRY_FACTORY_WM =
            new GeometryFactory(new PrecisionModel(), 3857);
    private static final GeometryFactory GEOMETRY_FACTORY_LV95 =
            new GeometryFactory(new PrecisionModel(), 2056);

    private JtsConverter() {}

    @NotNull
    public static Point createPointWgs84(final double x, final double y) {
        return GEOMETRY_FACTORY_WGS84.createPoint(new Coordinate(x, y));
    }

    @NotNull
    public static Point toJtsPoint(final WGS84Coordinate wgs84Coordinate) {
        return createPointWgs84(wgs84Coordinate.getLongitude(), wgs84Coordinate.getLatitude());
    }

    @NotNull
    public static WGS84Coordinate toWgs84(final Point point) {
        return new WGS84Coordinate(point.getX(), point.getY());
    }

    @NotNull
    public static Point createPointLv03(final double x, final double y) {
        return GEOMETRY_FACTORY_LV03.createPoint(new Coordinate(x, y));
    }

    @NotNull
    public static Point toJtsPoint(final LV03Coordinate lv03Coordinate) {
        return createPointLv03(lv03Coordinate.getEast(), lv03Coordinate.getNorth());
    }

    @NotNull
    public static LV03Coordinate toLv03(final Point point) {
        return new LV03Coordinate(point.getX(), point.getY());
    }

    @NotNull
    public static Point createPointWebMercator(final double x, final double y) {
        return GEOMETRY_FACTORY_WM.createPoint(new Coordinate(x, y));
    }

    @NotNull
    public static Point toJtsPoint(final WebMercatorCoordinate webMercatorCoordinate) {
        return createPointWebMercator(webMercatorCoordinate.getX(), webMercatorCoordinate.getY());
    }

    @NotNull
    public static WebMercatorCoordinate toWebMercator(final Point point) {
        return new WebMercatorCoordinate(point.getX(), point.getY());
    }

    @NotNull
    public static Point createPointLv95(final double x, final double y) {
        return GEOMETRY_FACTORY_LV95.createPoint(new Coordinate(x, y));
    }

    @NotNull
    public static Point toJtsPoint(final LV95Coordinate lv95Coordinate) {
        return createPointLv95(lv95Coordinate.getEast(), lv95Coordinate.getNorth());
    }

    @NotNull
    public static LV95Coordinate toLv95(final Point point) {
        return new LV95Coordinate(point.getX(), point.getY());
    }
}
