package ch.ubique.backend.geo.coordinate.constants;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WEB_MERCATOR_MAX_EXTENT;

import ch.ubique.backend.geo.coordinate.models.*;

/**
 * Same bounds as MapSDK: <a
 * href="https://github.com/openmobilemaps/maps-core/blob/5c91e2082ec163e597458eb56aabe677ab2b8e5c/shared/src/map/coordinates/CoordinateSystemFactory.cpp">CoordinateSystemFactory.cpp</a>
 */
public final class Constants {

    /** EPSG 2056 / LV95: About Switzerland + 5% Margin on each side */
    public static final BoundingBox<LV95Coordinate> BOUNDING_BOX_SWITZERLAND_LV95 =
            new BoundingBox<>(
                    new LV95Coordinate(2_485_000.0, 1_070_000.0),
                    new LV95Coordinate(2_840_000.0, 1_300_000.0));

    /** EPSG 21781 / LV03: About Switzerland + 5% Margin on each side */
    public static final BoundingBox<LV03Coordinate> BOUNDING_BOX_SWITZERLAND_LV03 =
            new BoundingBox<>(
                    new LV03Coordinate(485_000.0, 70_000.0),
                    new LV03Coordinate(840_000.0, 300_000.0));

    /** EPSG 4326 / WGS 84 */
    public static final BoundingBox<WGS84Coordinate> BOUNDING_BOX_SWITZERLAND_WGS84 =
            new BoundingBox<>(new WGS84Coordinate(-90.0, -180.0), new WGS84Coordinate(90.0, 180.0));

    /** EPSG 3857 / Web Mercator */
    public static final BoundingBox<WebMercatorCoordinate> BOUNDING_BOX_SWITZERLAND_WEB_MERCATOR =
            new BoundingBox<>(
                    new WebMercatorCoordinate(-WEB_MERCATOR_MAX_EXTENT, -WEB_MERCATOR_MAX_EXTENT),
                    new WebMercatorCoordinate(WEB_MERCATOR_MAX_EXTENT, WEB_MERCATOR_MAX_EXTENT));

    private Constants() {}
}
