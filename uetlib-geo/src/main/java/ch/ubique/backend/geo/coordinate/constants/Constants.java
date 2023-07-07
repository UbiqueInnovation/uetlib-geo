package ch.ubique.backend.geo.coordinate.constants;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

public final class Constants {

    /** About Switzerland + 5% Margin on each side */
    public static final BoundingBox<LV95Coordinate> BOUNDING_BOX_SWITZERLAND_LV95 =
            new BoundingBox<>(
                    new LV95Coordinate(2480000, 1062000), new LV95Coordinate(2843000, 1302000));

    private Constants() {}
}
