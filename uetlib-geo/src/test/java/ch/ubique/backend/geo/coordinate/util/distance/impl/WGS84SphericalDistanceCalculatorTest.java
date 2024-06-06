package ch.ubique.backend.geo.coordinate.util.distance.impl;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;

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
}
