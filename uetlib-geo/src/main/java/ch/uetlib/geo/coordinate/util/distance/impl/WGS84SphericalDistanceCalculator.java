package ch.uetlib.geo.coordinate.util.distance.impl;

import static java.lang.Math.*;

import ch.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.coordinate.models.WGS84Coordinate;
import ch.uetlib.geo.coordinate.util.JtsUtil;
import ch.uetlib.geo.coordinate.util.distance.DistanceCalculator;

import org.apache.commons.lang3.stream.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.geometry.jts.ReferencedEnvelope;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

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
        return 2 * asin(sqrt(haversineTheta(c1, c2)));
    }

    public static double haversine(final double alpha) {
        final var haversineSqrt = sin(alpha / 2);
        return haversineSqrt * haversineSqrt;
    }

    public static double haversineTheta(final WGS84Coordinate c1, final WGS84Coordinate c2) {
        final var latRadians1 = toRadians(c1.latitude());
        final var latRadians2 = toRadians(c2.latitude());
        final var latDistance = latRadians2 - latRadians1;
        final var lonDistance = toRadians(c2.longitude() - c1.longitude());
        return haversine(latDistance)
                + cos(latRadians1) * cos(latRadians2) * haversine(lonDistance);
    }

    public static Comparator<WGS84Coordinate> distanceComparator(final WGS84Coordinate reference) {
        return Comparator.comparingDouble(distanceComparisonKey(reference));
    }

    public static ToDoubleFunction<WGS84Coordinate> distanceComparisonKey(
            final WGS84Coordinate reference) {

        // precompute common terms
        final var refLatRad = toRadians(reference.latitude());
        final var refLonRad = toRadians(reference.longitude());
        final var cosRefLat = cos(refLatRad);

        // Compute value for comparison
        return c -> {
            final var latRad = toRadians(c.latitude());
            final var lonRad = toRadians(c.longitude());
            final var latDistance = latRad - refLatRad;
            final var lonDistance = lonRad - refLonRad;

            return haversine(latDistance) + cosRefLat * cos(latRad) * haversine(lonDistance);
        };
    }

    public static Stream<WGS84Coordinate> streamSortedByDistance(
            final Iterable<WGS84Coordinate> coordinates, final WGS84Coordinate reference) {
        final var keyFunc = distanceComparisonKey(reference);
        return Streams.of(coordinates)
                .map(c -> Pair.of(c, keyFunc.applyAsDouble(c)))
                .sorted(Comparator.comparingDouble(Pair::getValue))
                .map(Pair::getKey);
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
