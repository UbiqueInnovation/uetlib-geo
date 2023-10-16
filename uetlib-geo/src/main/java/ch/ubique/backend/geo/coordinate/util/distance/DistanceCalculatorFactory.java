package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

public class DistanceCalculatorFactory {

    private DistanceCalculatorFactory() {}

    public static DistanceCalculator create(final CoordinateReferenceSystem crs) {
        if (crs.equals(CoordinateSystems.LV95.crs()) || crs.equals(CoordinateSystems.LV03.crs())) {
            return new SwissDistanceCalculator();
        }
        return new GenericDistanceCalculator(crs);
    }
}
