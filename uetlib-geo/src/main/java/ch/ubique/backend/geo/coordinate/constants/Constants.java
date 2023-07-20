package ch.ubique.backend.geo.coordinate.constants;

import ch.ubique.backend.geo.coordinate.models.*;

/**
 * Same bounds as MapSDK: <a
 * href="https://github.com/openmobilemaps/maps-core/blob/5c91e2082ec163e597458eb56aabe677ab2b8e5c/shared/src/map/coordinates/CoordinateSystemFactory.cpp">CoordinateSystemFactory.cpp</a>
 */
public final class Constants {

    /**
     * EPSG 2056 / LV95: Switzerland + 5% Margin on each side: 15k in east/west direction and 10k in
     * north/south
     */
    public static final BoundingBox<LV95Coordinate> BOUNDING_BOX_SWITZERLAND_LV95 =
            new BoundingBox<>(
                    new LV95Coordinate(2_470_000.0, 1_060_000.0),
                    new LV95Coordinate(2_855_000.0, 1_310_000.0));

    /** EPSG 2056 / LV95 */
    public static final BoundingBox<LV95Coordinate> BOUNDING_BOX_SWITZERLAND_TIGHT_LV95 =
            new BoundingBox<>(
                    new LV95Coordinate(2_485_000.0, 1_070_000.0),
                    new LV95Coordinate(2_840_000.0, 1_300_000.0));

    /** EPSG 21781 / LV03: Switzerland + 5% Margin on each side */
    public static final BoundingBox<LV03Coordinate> BOUNDING_BOX_SWITZERLAND_LV03 =
            new BoundingBox<>(
                    new LV03Coordinate(470_000.0, 60_000.0),
                    new LV03Coordinate(855_000.0, 310_000.0));

    /** EPSG 21781 / LV03 */
    public static final BoundingBox<LV03Coordinate> BOUNDING_BOX_SWITZERLAND_TIGHT_LV03 =
            new BoundingBox<>(
                    new LV03Coordinate(485_000.0, 70_000.0),
                    new LV03Coordinate(840_000.0, 300_000.0));

    private Constants() {}
}
