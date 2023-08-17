package ch.ubique.backend.geo.coordinate.constants;

import static ch.ubique.backend.core.utils.DoubleUtils.nullIfNaN;

import static org.locationtech.jts.geom.Coordinate.NULL_ORDINATE;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;

import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.stream.Stream;

public class CoordinateSystems {

    private CoordinateSystems() {}

    public static final CoordinateSystem<WGS84Coordinate> WGS84 =
            new CoordinateSystem<>(
                    4326,
                    c ->
                            new org.locationtech.jts.geom.Coordinate(
                                    c.getLongitude(),
                                    c.getLatitude(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new WGS84Coordinate(c.y, c.x, nullIfNaN(c.getZ())),
                    new WGS84Coordinate(-90, -180),
                    new WGS84Coordinate(90, 180));
    /**
     * bounds equal to the min/max available coordinates with tiles on <a
     * href="https://map.geo.admin.ch/">map.geo.admin.ch</a>
     */
    public static final CoordinateSystem<LV95Coordinate> LV95 =
            new CoordinateSystem<>(
                    2056,
                    c ->
                            new org.locationtech.jts.geom.Coordinate(
                                    c.getEast(),
                                    c.getNorth(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new LV95Coordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new LV95Coordinate(2_420_000.0, 1_000_000.0),
                    new LV95Coordinate(2_900_000.0, 1_350_000.0));
    /**
     * bounds equal to the min/max available coordinates with tiles on <a
     * href="https://map.geo.admin.ch/">map.geo.admin.ch</a>
     */
    public static final CoordinateSystem<LV03Coordinate> LV03 =
            new CoordinateSystem<>(
                    21781,
                    c ->
                            new org.locationtech.jts.geom.Coordinate(
                                    c.getEast(),
                                    c.getNorth(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new LV03Coordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new LV03Coordinate(420_000.0, 0.0),
                    new LV03Coordinate(900_000.0, 350_000.0));
    // See https://schemas.opengis.net/wmts/1.0/profiles/wmts-simple/wmtsTileMatrixSet.json
    private static final double WEB_MERCATOR_MAX_EXTENT = 20_037_508.3427892;
    public static final CoordinateSystem<WebMercatorCoordinate> WEB_MERCATOR =
            new CoordinateSystem<>(
                    3857,
                    c ->
                            new Coordinate(
                                    c.getX(),
                                    c.getY(),
                                    c.getOptionalElevation().orElse(NULL_ORDINATE)),
                    c -> new WebMercatorCoordinate(c.x, c.y, nullIfNaN(c.getZ())),
                    new WebMercatorCoordinate(-WEB_MERCATOR_MAX_EXTENT, -WEB_MERCATOR_MAX_EXTENT),
                    new WebMercatorCoordinate(WEB_MERCATOR_MAX_EXTENT, WEB_MERCATOR_MAX_EXTENT));

    @SuppressWarnings("java:S1452") // No way of knowing the generic type here
    public static CoordinateSystem<?> forCrs(final CoordinateReferenceSystem crs) {
        return Stream.of(LV95, LV03, WGS84, WEB_MERCATOR)
                .filter(coordinateSystem -> coordinateSystem.crs().equals(crs))
                .findFirst()
                .orElseThrow();
    }
}
