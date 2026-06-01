package ch.ubique.backend.geo.utils.duration;

import static ch.ubique.backend.geo.utils.duration.HikeDuration.calculateDuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class HikeDurationTest {

    @Test
    void test() {
        assertEquals(Duration.ZERO, calculateDuration(0, 0));
        assertEquals(Duration.ZERO, calculateDuration(0, 10));
        final var durationForFlatKilometer = Duration.ofMinutes(14).plusSeconds(16).plusMillis(260);
        assertEquals(durationForFlatKilometer, calculateDuration(1000, 0));
        assertEquals(durationForFlatKilometer.multipliedBy(2), calculateDuration(2000, 0));
    }

    @Test
    void testPace() {
        for (var slope = -1.0; slope <= 1.0; slope += 0.01) {
            final var pace = HikeDuration.pace(slope);
            assertThat(pace).isPositive();
            System.out.println(slope + "," + pace);
        }
    }
}
