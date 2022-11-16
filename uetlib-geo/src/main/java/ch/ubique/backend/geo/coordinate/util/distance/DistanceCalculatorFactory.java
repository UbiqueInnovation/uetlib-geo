package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class DistanceCalculatorFactory {

    private DistanceCalculatorFactory() {}

    public static DistanceCalculator create(CoordinateReferenceSystem crs) {
        if (crs.equals(JtsConverter.LV95.crs()) || crs.equals(JtsConverter.LV03.crs())) {
            return new SwissDistanceCalculator();
        }
        return new GenericDistanceCalculator(crs);
    }
}
