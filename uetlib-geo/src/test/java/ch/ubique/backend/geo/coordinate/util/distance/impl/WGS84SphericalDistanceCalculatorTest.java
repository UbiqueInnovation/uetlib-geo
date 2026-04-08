package ch.ubique.backend.geo.coordinate.util.distance.impl;

import static ch.ubique.backend.geo.coordinate.util.distance.impl.WGS84SphericalDistanceCalculator.*;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

class WGS84SphericalDistanceCalculatorTest {

    private final Random random = new Random();

    @Test
    void compareToReferenceImplementation() {
        final var referenceCalculator = new GenericDistanceCalculator<>(CoordinateSystems.WGS84);
        final var wgs84DistanceCalculator = new WGS84SphericalDistanceCalculator();
        for (var i = 0; i < 1000; i++) {
            final var c1 = randomCoordinate();
            final var c2 = randomCoordinate();
            final var referenceDistance = referenceCalculator.getDistance(c1, c2);
            final var actualDistance = wgs84DistanceCalculator.getDistance(c1, c2);
            // So long as a spherical Earth is assumed, any single formula for distance on the
            // Earth is only guaranteed correct within 0.5%
            final var delta = referenceDistance * 0.01;
            assertEquals(referenceDistance, actualDistance, delta);
        }
    }

    @Test
    void testPerformance() {
        final var numSamples = 10_000_000;
        final var randomCoordinates =
                Stream.generate(this::randomCoordinate)
                        .limit(numSamples)
                        .toArray(WGS84Coordinate[]::new);

        final var start = System.nanoTime();
        for (var i = 0; i < numSamples; ++i) {
            final var c1 = randomCoordinates[i];
            final var c2 = randomCoordinates[numSamples - i - 1];
            distanceInMeters(c1, c2);
        }
        final var end = System.nanoTime();
        final var durationInNanos = end - start;
        final var durationPerCalculation = 1.0 * durationInNanos / numSamples;
        System.out.printf(
                "Total duration %d ns, duration per calculation: %f ns",
                durationInNanos, durationPerCalculation);
    }

    @Test
    void testComparator() {
        for (var i = 0; i < 1_000; ++i) {
            final var reference = randomCoordinate();
            final var comparator = distanceComparator(reference);
            final var meterComparator =
                    Comparator.comparingDouble(
                            (final WGS84Coordinate c) -> distanceInMeters(reference, c));
            for (var j = 0; j < 10_000; ++j) {
                final var c1 = randomCoordinate();
                final var c2 = randomCoordinate();
                final var meterComparison = meterComparator.compare(c1, c2);
                final var directComparison = comparator.compare(c1, c2);
                assertEquals(meterComparison, directComparison);
            }
        }
    }

    @Test
    void testSorting() {
        for (var i = 0; i < 100; ++i) {
            final var reference = randomCoordinate();
            final var unsorted = Stream.generate(this::randomCoordinate).limit(10_000).toList();
            final var withUtil = streamSortedByDistance(unsorted, reference).toList();
            final var withComparator =
                    unsorted.stream().sorted(distanceComparator(reference)).toList();
            assertEquals(withComparator, withUtil);
        }
    }

    private WGS84Coordinate randomCoordinate() {
        return new WGS84Coordinate(random.nextDouble(-90, 90), random.nextDouble(-180, 180));
    }

    @Test
    void testMetersToDegreeLat() {
        assertEquals(0, metersToDegreeLat(0));
        assertEquals(1, metersToDegreeLat(111_195), 1.0e-6);
        assertEquals(8.993e-5, metersToDegreeLat(10), 1.0e-7);
    }

    @Test
    void testMetersToDegreeLon() {
        final var c1 = new WGS84Coordinate(0, 0);
        final var c2 = new WGS84Coordinate(42, 0);
        assertEquals(0, metersToDegreeLon(c1, 0));
        assertEquals(1, metersToDegreeLon(c1, 111_195), 1.0e-6);
        assertEquals(1, metersToDegreeLon(c2, 82_634), 1.0e-6);
        assertEquals(1, metersToDegreeLon(c2.latitude(), 82_634), 1.0e-6);
        assertTrue(metersToDegreeLon(90, 1) > 360);
    }

    @Test
    void testMetersToDegreeUpperBound() {
        assertEquals(0, metersToDegreeUpperBound(0));
        assertEquals(1, metersToDegreeUpperBound(111_195), 1.0e-6);
        assertEquals(8.993e-5, metersToDegreeLat(10), 1.0e-8);
        assertEquals(8.993e-4, metersToDegreeLat(100), 1.0e-7);
        assertEquals(8.993e-3, metersToDegreeLat(1_000), 1.0e-6);
    }

    @ParameterizedTest
    @CsvSource({
        "0,0,5,8.993e-5,8.993e-5",
        "90,0,10,360,1.799e-4",
        "90,180,10,360,1.799e-4",
        "45,0,10,2.543e-4,1.799e-4",
        "47.37,8.55,10,2.655e-4,1.799e-4",
    })
    void testEnvelopeWithBufferInMeters(
            final double lat,
            final double lon,
            final double buffer,
            final double width,
            final double height) {
        final var coordinate = new WGS84Coordinate(lat, lon);
        final var envelope = envelopeWithBufferInMeters(coordinate, buffer);
        assertEquals(coordinate.toJts(), envelope.centre());
        assertEquals(width, envelope.getWidth(), 1.0e-6);
        assertEquals(height, envelope.getHeight(), 1.0e-6);
    }

    @Test
    void testBearingAngle() {
        assertEquals(
                Math.toRadians(0),
                bearingAngle(new WGS84Coordinate(0, 0), new WGS84Coordinate(90, 0)),
                1.0e-6);
        assertEquals(
                Math.toRadians(90),
                bearingAngle(new WGS84Coordinate(0, 0), new WGS84Coordinate(0, -90)),
                1.0e-6);
        assertEquals(
                Math.toRadians(180),
                bearingAngle(new WGS84Coordinate(90, 0), new WGS84Coordinate(0, 0)),
                1.0e-6);
        assertEquals(
                Math.toRadians(270),
                bearingAngle(new WGS84Coordinate(0, 0), new WGS84Coordinate(0, 90)),
                1.0e-6);
        assertEquals(
                Math.toRadians(45),
                bearingAngle(new WGS84Coordinate(0, 0), new WGS84Coordinate(0.01, -0.01)),
                1.0e-4);
        final var c1 = new WGS84Coordinate(35, 45);
        final var c2 = new WGS84Coordinate(35, 135);
        assertEquals(Math.toRadians(360 - 60), bearingAngle(c1, c2), 0.01);
        assertEquals(Math.toRadians(60), bearingAngle(c2, c1), 0.01);

        assertEquals(
                Math.toRadians(340),
                bearingAngle(
                        new WGS84Coordinate(47.375597998969575, 8.54359878165179),
                        new WGS84Coordinate(47.37567973526378, 8.543643038100736)),
                Math.toRadians(10));

        assertEquals(
                Math.toRadians(90),
                bearingAngle(
                        new WGS84Coordinate(47.375597998969575, 8.54359878165179),
                        new WGS84Coordinate(47.375591187606005, 8.543311114733646)),
                Math.toRadians(10));

        assertEquals(
                Math.toRadians(45),
                bearingAngle(
                        new WGS84Coordinate(47.375597998969575, 8.54359878165179),
                        new WGS84Coordinate(47.37567519436189, 8.543494175499738)),
                Math.toRadians(10));
    }
}
