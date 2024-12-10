package ch.ubique.backend.geo.coordinate.util.distance.impl;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
            WGS84SphericalDistanceCalculator.greatCircleDistance(c1, c2);
        }
        final var end = System.nanoTime();
        final var durationInNanos = end - start;
        final var durationPerCalculation = 1.0 * durationInNanos / numSamples;
        System.out.printf(
                "Total duration %d ns, duration per calculation: %f ns",
                durationInNanos, durationPerCalculation);
    }

    private WGS84Coordinate randomCoordinate() {
        return new WGS84Coordinate(random.nextDouble(-90, 90), random.nextDouble(-180, 180));
    }

    @Test
    void metersToDegreeLat() {
        assertEquals(0, WGS84SphericalDistanceCalculator.metersToDegreeLat(0));
        assertEquals(1, WGS84SphericalDistanceCalculator.metersToDegreeLat(111_195), 1.0e-6);
        assertEquals(8.993e-5, WGS84SphericalDistanceCalculator.metersToDegreeLat(10), 1.0e-7);
    }

    @Test
    void metersToDegreeLon() {
        final var c1 = new WGS84Coordinate(0, 0);
        final var c2 = new WGS84Coordinate(42, 0);
        assertEquals(0, WGS84SphericalDistanceCalculator.metersToDegreeLon(c1, 0));
        assertEquals(1, WGS84SphericalDistanceCalculator.metersToDegreeLon(c1, 111_195), 1.0e-6);
        assertEquals(1, WGS84SphericalDistanceCalculator.metersToDegreeLon(c2, 82_634), 1.0e-6);
        assertEquals(
                1,
                WGS84SphericalDistanceCalculator.metersToDegreeLon(c2.latitude(), 82_634),
                1.0e-6);
        assertTrue(WGS84SphericalDistanceCalculator.metersToDegreeLon(90, 1) > 360);
    }

    @Test
    void metersToDegreeUpperBound() {
        assertEquals(0, WGS84SphericalDistanceCalculator.metersToDegreeUpperBound(0));
        assertEquals(1, WGS84SphericalDistanceCalculator.metersToDegreeUpperBound(111_195), 1.0e-6);
        assertEquals(8.993e-5, WGS84SphericalDistanceCalculator.metersToDegreeLat(10), 1.0e-8);
        assertEquals(8.993e-4, WGS84SphericalDistanceCalculator.metersToDegreeLat(100), 1.0e-7);
        assertEquals(8.993e-3, WGS84SphericalDistanceCalculator.metersToDegreeLat(1_000), 1.0e-6);
    }

    @ParameterizedTest
    @CsvSource({
        "0,0,5,8.993e-5,8.993e-5",
        "90,0,10,360,1.799e-4",
        "90,180,10,360,1.799e-4",
        "45,0,10,2.543e-4,1.799e-4",
        "47.37,8.55,10,2.655e-4,1.799e-4",
    })
    void envelopeWithBufferInMeters(
            final double lat,
            final double lon,
            final double buffer,
            final double width,
            final double height) {
        final var coordinate = new WGS84Coordinate(lat, lon);
        final var envelope =
                WGS84SphericalDistanceCalculator.envelopeWithBufferInMeters(coordinate, buffer);
        assertEquals(coordinate.toJts(), envelope.centre());
        assertEquals(width, envelope.getWidth(), 1.0e-6);
        assertEquals(height, envelope.getHeight(), 1.0e-6);
    }
}
