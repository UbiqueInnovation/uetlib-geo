package ch.ubique.backend.geo.coordinate.util.distance.impl;

import static java.lang.Math.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculator;

/**
 * This calculator assumes a spherical earth. This implies an error of up to 0.5% compared to
 * distance computations on the actual WGS84 ellipsoid
 */
public class WGS84SphericalDistanceCalculator implements DistanceCalculator<WGS84Coordinate> {

    /** The mean Earth radius for the WGS84 ellipsoid. */
    public static final double MEAN_EARTH_RADIUS = 6371_009;

    @Override
    public double getDistance(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        return distanceInMeters(c1, c2);
    }

    @Override
    public CoordinateSystem<WGS84Coordinate> coordinateSystem() {
        return CoordinateSystems.WGS84;
    }

    public static double distanceInMeters(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        return greatCircleDistance(c1, c2) * MEAN_EARTH_RADIUS;
    }

    public static double distanceInDegrees(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        return toDegrees(greatCircleDistance(c1, c2));
    }

    public static double greatCircleDistance(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        final var latDistance = toRadians(c2.latitude() - c1.latitude());
        final var lonDistance = toRadians(c2.longitude() - c1.longitude());
        final var a =
                pow(sin(latDistance / 2), 2)
                        + cos(toRadians(c1.latitude()))
                                * cos(toRadians(c2.latitude()))
                                * pow(sin(lonDistance / 2), 2);
        return 2 * asin(sqrt(a));
    }
}
