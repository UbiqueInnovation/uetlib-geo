package ch.ubique.backend.geo.utils.duration;

import ch.ubique.backend.core.utils.DurationUtils;
import java.time.Duration;

public class SkiTourDuration {

    private SkiTourDuration() {}

    public static Duration calculateDuration(final double distance, final double elevationDiff) {
        if (distance < 0) {
            throw new IllegalArgumentException("The distance must not be negative.");
        }

        final var offsetFactor = 0.75;
        final var durationAscent =
                (distance / 40 + elevationDiff / (400.0 / 60)) * 60 * offsetFactor;

        if (elevationDiff >= 0) {
            return DurationUtils.ofFractionalSeconds(durationAscent);
        }

        // descent is ~1/3 of ascent
        return DurationUtils.ofFractionalSeconds(durationAscent / 3);
    }
}
