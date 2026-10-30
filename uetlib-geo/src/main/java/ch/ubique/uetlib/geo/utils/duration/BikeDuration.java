package ch.ubique.uetlib.geo.utils.duration;

import ch.ubique.uetlib.core.utils.DurationUtils;
import java.time.Duration;
import org.jspecify.annotations.Nullable;

public class BikeDuration {

    private BikeDuration() {}

    public static Duration calculateDuration(
            final double distance, final double elevationDiff, final BikeType bikeType) {
        return calculateDuration(distance, elevationDiff, null, bikeType);
    }

    public static Duration calculateDuration(
            final double distance,
            final double elevationDiff,
            final @Nullable Double speed,
            final BikeType bikeType) {

        if (distance == 0) {
            return Duration.ZERO;
        }

        final var vh_kmh = vhKmh(speed, bikeType);

        // velocity in in m/s
        final var vh = vh_kmh / 3.6;

        // compute cw.A (Fahrerhaltung) based on velocity (in km/h)
        final var cwa = cwa(vh_kmh);

        // Constants
        final var p = 1.22601; // Luftdichte
        final var m = 90; // Masse
        final var g = 9.8067; // Erdbeschleundigung
        final var cr =
                bikeType == BikeType.MOUNTAIN_BIKE
                        ? 0.005
                        : 0.004; // Rollwiderstandskoeffizient, zwischen 0.003 (Rennreifen) und
        // 0.005 (Stollenreifen)

        // compute ph (Power horizontal, Leistung in der Ebene)
        // ph = (0.5.cW.A.p.vh2 + m.g.cr). vh
        final var fDrag = 0.5 * cwa * p * vh * vh;
        final var fRolling = m * g * cr;
        final var fResist = fRolling + fDrag;
        final var ph = fResist * vh;

        // Compute b (Steigung in radians)
        final var b = Math.atan(elevationDiff / distance);

        // compute P (Steigungsabhängige Leistung)
        final double pFac;
        final var bPZero = -0.08;
        final var bPMax = 0.08;
        final var PMax = 1.3;
        if (b < bPZero) {
            pFac = 0;
        } else if (b <= 0) {
            pFac = (-bPZero + b) / -bPZero;
        } else if (b > bPMax) {
            pFac = PMax;
        } else {
            pFac = 1.0 + b / bPMax * (PMax - 1.0);
        }
        final var P = ph * pFac;

        // Newton Iterations
        // Start with high value (50) because there can be multiple solutions
        // WolframAlpha example: 0.50108233947491732 *
        // 0.5*x^3+90*9.81*(-0.71+0.005)*x-1889.5178760821132=0
        var vn = 50.0;
        final var maxIter = 50;
        for (int i = 0; i <= maxIter; i++) { // Newton
            final var v = vn;
            final var fv = 0.5 * cwa * p * v * v * v + m * g * (b + cr) * v - P;
            final var ffv = 1.5 * cwa * p * v * v + m * g * (b + cr);
            var delta = fv / ffv;
            if (delta < -1.0) {
                delta = -1.0;
            } else if (delta > 1.0) {
                delta = 1.0;
            } else if (Math.abs(delta) < 0.01) {
                break;
            }
            vn = vn - delta;
        }

        // Geschwindigkeitsbegrenzung
        if (vn < 3.0 / 3.6) {
            vn = 3.0 / 3.6;
        } else if (vn > 50.0 / 3.6) {
            vn = 50.0 / 3.6;
        }

        return DurationUtils.ofFractionalSeconds(distance / vn);
    }

    private static double vhKmh(final @Nullable Double speed, final BikeType bikeType) {
        final var defaultSpeed = (bikeType == BikeType.MOUNTAIN_BIKE) ? 16.0 : 18.0;

        // Use the default speed if input is null
        if (speed == null) {
            return defaultSpeed;
        }

        // Round close values to the default speed
        if (Math.abs((speed - defaultSpeed) / defaultSpeed) < 0.05) {
            return defaultSpeed;
        }

        return speed;
    }

    private static double cwa(final double vh_kmh) {
        if (vh_kmh <= 10) {
            return 0.625;
        }
        if (vh_kmh >= 35) {
            return 0.25;
        }
        return 0.625 - (vh_kmh - 10.0) * (0.625 - 0.25) / (35.0 - 10.0);
    }
}
