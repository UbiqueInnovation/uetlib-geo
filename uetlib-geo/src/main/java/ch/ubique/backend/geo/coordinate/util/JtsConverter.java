package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import javax.validation.constraints.NotNull;

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
    public static Point createPoint(WGS84Coordinate wgs84Coordinate) {
        return GEOMETRY_FACTORY_WGS84.createPoint(toJts(wgs84Coordinate));
    }

    @NotNull
    public static Point toJtsPoint(final WGS84Coordinate wgs84Coordinate) {
        return createPoint(wgs84Coordinate);
    }

    @NotNull
    public static Coordinate toJts(final WGS84Coordinate coordinate) {
        return new Coordinate(coordinate.getLongitude(), coordinate.getLatitude());
    }

    @NotNull
    public static WGS84Coordinate toWgs84(final Point point) {
        return new WGS84Coordinate(point.getY(), point.getX());
    }

    @NotNull
    public static Point createPoint(LV03Coordinate coordinate) {
        return GEOMETRY_FACTORY_LV03.createPoint(toJts(coordinate));
    }

    @NotNull
    public static Coordinate toJts(final LV03Coordinate lv03Coordinate) {
        return new Coordinate(lv03Coordinate.getEast(), lv03Coordinate.getNorth());
    }

    @NotNull
    public static Point toJtsPoint(final LV03Coordinate lv03Coordinate) {
        return createPoint(lv03Coordinate);
    }

    @NotNull
    public static LV03Coordinate toLv03(final Point point) {
        return new LV03Coordinate(point.getX(), point.getY());
    }

    @NotNull
    public static Point createPoint(WebMercatorCoordinate coordinate) {
        return GEOMETRY_FACTORY_WM.createPoint(toJts(coordinate));
    }

    @NotNull
    public static Coordinate toJts(final WebMercatorCoordinate coordinate) {
        return new Coordinate(coordinate.getX(), coordinate.getY());
    }

    @NotNull
    public static Point toJtsPoint(final WebMercatorCoordinate webMercatorCoordinate) {
        return createPoint(webMercatorCoordinate);
    }

    @NotNull
    public static WebMercatorCoordinate toWebMercator(final Point point) {
        return new WebMercatorCoordinate(point.getX(), point.getY());
    }

    @NotNull
    public static Point createPoint(LV95Coordinate coordinate) {
        return GEOMETRY_FACTORY_LV95.createPoint(toJts(coordinate));
    }

    @NotNull
    public static Coordinate toJts(final LV95Coordinate lv03Coordinate) {
        return new Coordinate(lv03Coordinate.getEast(), lv03Coordinate.getNorth());
    }

    @NotNull
    public static Point toJtsPoint(final LV95Coordinate coordinate) {
        return createPoint(coordinate);
    }

    @NotNull
    public static LV95Coordinate toLv95(final Point point) {
        return new LV95Coordinate(point.getX(), point.getY());
    }
}
