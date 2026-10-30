package ch.ubique.uetlib.geo.coordinate.util;

/**
 * Converts angular units used by GPS protocols. One semicircle is 180 degrees or pi radians.
 *
 * <p>The methods ending in {@code Semicircles32} use a signed 32-bit fixed-point representation
 * with 2^31 units per semicircle, as used by Garmin positions and FIT coordinates. Other GPS
 * protocols may use different scales; the double-valued unit conversions do not assume a scale.
 * These conversions do not change the coordinate reference system.
 */
public final class SemicircleUtil {

    private static final double UNITS_PER_SEMICIRCLE_32 = 0x1.0p31;

    private SemicircleUtil() {}

    /** Converts degrees to semicircle units (180 degrees equals 1 semicircle). */
    public static double degreesToSemicircles(final double degrees) {
        return degrees / 180.0;
    }

    /** Converts semicircle units to degrees. */
    public static double semicirclesToDegrees(final double semicircles) {
        return semicircles * 180.0;
    }

    /** Converts radians to semicircle units (pi radians equals 1 semicircle). */
    public static double radiansToSemicircles(final double radians) {
        return radians / Math.PI;
    }

    /** Converts semicircle units to radians. */
    public static double semicirclesToRadians(final double semicircles) {
        return semicircles * Math.PI;
    }

    /**
     * Encodes an angle in signed 32-bit semicircles, truncating towards zero.
     *
     * <p>Both -180 and +180 degrees encode as {@link Integer#MIN_VALUE}, which decodes to -180
     * degrees. This is an angular conversion; callers must validate latitude bounds separately.
     * Protocol-specific reserved values (such as the FIT invalid-position value) are not handled.
     *
     * @throws IllegalArgumentException if the angle is non-finite or outside [-180, 180] degrees
     */
    public static int degreesToSemicircles32(final double degrees) {
        requireAngleInRange(degrees, 180.0);
        // Narrow via long so +180 wraps instead of saturating to Integer.MAX_VALUE.
        return (int) (long) (degrees * (UNITS_PER_SEMICIRCLE_32 / 180.0));
    }

    /**
     * Decodes signed 32-bit semicircles to degrees in [-180, 180). All integer values are
     * interpreted as angles, including protocol-specific reserved values.
     */
    public static double semicircles32ToDegrees(final int semicircles) {
        return semicircles * (180.0 / UNITS_PER_SEMICIRCLE_32);
    }

    /**
     * Encodes an angle in signed 32-bit semicircles, truncating towards zero. Both -pi and +pi
     * encode as {@link Integer#MIN_VALUE}, which decodes to -pi.
     *
     * @throws IllegalArgumentException if the angle is non-finite or outside [-pi, pi] radians
     * @see #degreesToSemicircles32(double)
     */
    public static int radiansToSemicircles32(final double radians) {
        requireAngleInRange(radians, Math.PI);
        return (int) (long) (radiansToSemicircles(radians) * UNITS_PER_SEMICIRCLE_32);
    }

    /**
     * Decodes signed 32-bit semicircles to radians in [-pi, pi). All integer values are interpreted
     * as angles, including protocol-specific reserved values.
     */
    public static double semicircles32ToRadians(final int semicircles) {
        return semicircles * (Math.PI / UNITS_PER_SEMICIRCLE_32);
    }

    private static void requireAngleInRange(final double angle, final double halfTurn) {
        if (!Double.isFinite(angle) || angle < -halfTurn || angle > halfTurn) {
            throw new IllegalArgumentException(
                    "Angle must be finite and between " + -halfTurn + " and " + halfTurn);
        }
    }
}
