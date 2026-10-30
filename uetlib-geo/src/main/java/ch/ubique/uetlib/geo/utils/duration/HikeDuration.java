package ch.ubique.uetlib.geo.utils.duration;

import ch.ubique.uetlib.core.utils.DurationUtils;
import java.time.Duration;

public class HikeDuration {

    private static final double[] CHMOB_CONSTANTS = {
        14.271,
        3.6991,
        2.5922,
        -1.4384,
        0.32105,
        0.81542,
        -0.090261,
        -0.20757,
        0.010192,
        0.028588,
        -0.00057466,
        -0.0021842,
        1.5176e-5,
        8.6894e-5,
        -1.3584e-7,
        -1.4026e-6
    };

    private HikeDuration() {}

    /**
     * @param distance in meters [m]
     * @param elevationDiff in meters [m]
     */
    public static Duration calculateDuration(final double distance, final double elevationDiff) {
        if (distance < 0) {
            throw new IllegalArgumentException("The distance must not be negative.");
        }

        if (distance == 0) {
            return Duration.ZERO;
        }

        final var slope = elevationDiff / distance;
        final var minutesPerMeter = pace(slope) / 1000;
        final var duration = distance * minutesPerMeter * 60; // [s]

        return DurationUtils.ofFractionalSeconds(duration);
    }

    public static double pace(final double slopePercent) {

        // 10ths (schweizmobil formula) instead of % (official formula)
        final var slope = 10 * slopePercent;

        // The swiss hiking formula is used between -25% and +25%
        // but schweiz mobil use -40% and +40%
        if (slope > -4 && slope < 4) {
            double minutesPerKilometer = 0;
            double exp = 0;
            for (final var constant : CHMOB_CONSTANTS) {
                minutesPerKilometer += constant * Math.pow(slope, exp);
                exp += 1;
            }
            return minutesPerKilometer;
        }

        // outside the -40% to +40% range, we use a linear formula
        if (slope > 0) {
            return (17 * slope);
        } else {
            return (-9 * slope);
        }
    }
}
