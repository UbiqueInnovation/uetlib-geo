package ch.ubique.backend.geo.coordinate.util.distance.impl;

import static java.lang.Math.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculator;
import org.geotools.geometry.jts.ReferencedEnvelope;

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

    /**
     * @return Initial bearing angle (forward azimuth) from c1 to c2 in radians, counterclockwise, 0
     *     is up.
     */
    public static double bearingAngle(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        final var lon1 = toRadians(c1.longitude());
        final var lon2 = toRadians(c2.longitude());
        final var lat1 = toRadians(c1.latitude());
        final var lat2 = toRadians(c2.latitude());
        final var y = sin(lon2 - lon1) * cos(lat2);
        final var x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1);
        final var bearingClockwise = atan2(y, x);
        return (2 * PI - bearingClockwise) % (2 * PI); // Make it counterclockwise
    }

    public static double metersToDegreeLat(final double distance) {
        return toDegrees(distance / MEAN_EARTH_RADIUS);
    }

    public static double metersToDegreeLon(final WGS84Coordinate center, final double distance) {
        return metersToDegreeLon(center.latitude(), distance);
    }

    /**
     * @param referenceLatitude reference latitude for the calculation in degrees
     */
    public static double metersToDegreeLon(final double referenceLatitude, final double distance) {
        return toDegrees(distance / MEAN_EARTH_RADIUS) / cos(toRadians(referenceLatitude));
    }

    public static double metersToDegreeUpperBound(final double distance) {
        return metersToDegreeLat(distance);
    }

    public static ReferencedEnvelope envelopeWithBufferInMeters(
            final WGS84Coordinate center, final double distance) {
        final var envelope = JtsUtil.getEnvelope(center);
        final var deltaX = min(180, metersToDegreeLon(center, distance));
        final var deltaY = metersToDegreeLat(distance);
        envelope.expandBy(deltaX, deltaY);
        return envelope;
    }
}
