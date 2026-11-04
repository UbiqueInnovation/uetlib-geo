package ch.uetlib.geo.coordinate.util.distance;

import ch.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.coordinate.models.Coordinate;
import ch.uetlib.geo.coordinate.util.distance.impl.GenericDistanceCalculator;
import ch.uetlib.geo.coordinate.util.distance.impl.LV03DistanceCalculator;
import ch.uetlib.geo.coordinate.util.distance.impl.LV95DistanceCalculator;

public class DistanceCalculatorFactory {

    private DistanceCalculatorFactory() {}

    public static <C extends Coordinate> DistanceCalculator<C> create(
            final CoordinateSystem<C> coordinateSystem) {
        if (coordinateSystem.equals(CoordinateSystems.LV03)) {
            //noinspection unchecked
            return (DistanceCalculator<C>) new LV03DistanceCalculator();
        }
        if (coordinateSystem.equals(CoordinateSystems.LV95)) {
            //noinspection unchecked
            return (DistanceCalculator<C>) new LV95DistanceCalculator();
        }
        return new GenericDistanceCalculator<>(coordinateSystem);
    }
}
