package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

public class DistanceCalculators {

    private DistanceCalculators() {}

    public static final DistanceCalculator LV03 =
            DistanceCalculatorFactory.create(CoordinateSystems.LV03.crs());

    public static final DistanceCalculator LV95 =
            DistanceCalculatorFactory.create(CoordinateSystems.LV95.crs());

    public static final DistanceCalculator WGS84 =
            DistanceCalculatorFactory.create(CoordinateSystems.WGS84.crs());

    public static final DistanceCalculator WEB_MERCATOR =
            DistanceCalculatorFactory.create(CoordinateSystems.WEB_MERCATOR.crs());

    public static final DistanceCalculator UTM32 =
            DistanceCalculatorFactory.create(CoordinateSystems.UTM32.crs());
}
