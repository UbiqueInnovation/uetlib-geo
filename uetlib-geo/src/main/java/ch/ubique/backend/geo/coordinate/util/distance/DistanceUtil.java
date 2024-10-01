package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.Coordinate;

public final class DistanceUtil {

    private DistanceUtil() {}

    public static <C extends Coordinate> double calculateDistance(final C c1, final C c2) {
        @SuppressWarnings("unchecked")
        final var coordinateClass = (Class<C>) c1.getClass();
        final var distanceCalculator =
                CoordinateSystems.forClass(coordinateClass).distanceCalculator();
        return distanceCalculator.getDistance(c1, c2);
    }
}
