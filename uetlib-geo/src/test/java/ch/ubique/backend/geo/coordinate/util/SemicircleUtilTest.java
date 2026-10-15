package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemicircleUtilTest {

    private static final double DEGREES_PER_UNIT_32 = 180.0 / 0x1.0p31;
    private static final double RADIANS_PER_UNIT_32 = Math.PI / 0x1.0p31;

    @Test
    void convertsDegreesAndSemicircleUnits() {
        assertEquals(0.0, SemicircleUtil.degreesToSemicircles(0.0));
        assertEquals(0.25, SemicircleUtil.degreesToSemicircles(45.0));
        assertEquals(-0.5, SemicircleUtil.degreesToSemicircles(-90.0));
        assertEquals(1.0, SemicircleUtil.degreesToSemicircles(180.0));
        assertEquals(2.0, SemicircleUtil.degreesToSemicircles(360.0));
        assertEquals(45.0, SemicircleUtil.semicirclesToDegrees(0.25));
        assertEquals(-90.0, SemicircleUtil.semicirclesToDegrees(-0.5));
        assertEquals(360.0, SemicircleUtil.semicirclesToDegrees(2.0));
    }

    @Test
    void convertsRadiansAndSemicircleUnits() {
        assertEquals(0.0, SemicircleUtil.radiansToSemicircles(0.0));
        assertEquals(0.5, SemicircleUtil.radiansToSemicircles(Math.PI / 2.0));
        assertEquals(-1.0, SemicircleUtil.radiansToSemicircles(-Math.PI));
        assertEquals(2.0, SemicircleUtil.radiansToSemicircles(2.0 * Math.PI));
        assertEquals(Math.PI / 2.0, SemicircleUtil.semicirclesToRadians(0.5));
        assertEquals(-Math.PI, SemicircleUtil.semicirclesToRadians(-1.0));
        assertEquals(2.0 * Math.PI, SemicircleUtil.semicirclesToRadians(2.0));
    }

    @Test
    void encodesKnownGarminAngles() {
        assertEquals(0, SemicircleUtil.degreesToSemicircles32(0.0));
        assertEquals(0, SemicircleUtil.degreesToSemicircles32(-0.0));
        assertEquals(536_870_912, SemicircleUtil.degreesToSemicircles32(45.0));
        assertEquals(1_073_741_824, SemicircleUtil.degreesToSemicircles32(90.0));
        assertEquals(-1_073_741_824, SemicircleUtil.degreesToSemicircles32(-90.0));
        assertEquals(0, SemicircleUtil.radiansToSemicircles32(0.0));
        assertEquals(536_870_912, SemicircleUtil.radiansToSemicircles32(Math.PI / 4.0));
        assertEquals(-1_073_741_824, SemicircleUtil.radiansToSemicircles32(-Math.PI / 2.0));
    }

    @Test
    void decodesKnownGarminAngles() {
        assertEquals(0.0, SemicircleUtil.semicircles32ToDegrees(0));
        assertEquals(45.0, SemicircleUtil.semicircles32ToDegrees(536_870_912));
        assertEquals(-90.0, SemicircleUtil.semicircles32ToDegrees(-1_073_741_824));
        assertEquals(0.0, SemicircleUtil.semicircles32ToRadians(0));
        assertEquals(Math.PI / 4.0, SemicircleUtil.semicircles32ToRadians(536_870_912));
        assertEquals(-Math.PI / 2.0, SemicircleUtil.semicircles32ToRadians(-1_073_741_824));
    }

    @Test
    void encodesBothHalfTurnEndpointsAsNegativeHalfTurn() {
        assertEquals(Integer.MIN_VALUE, SemicircleUtil.degreesToSemicircles32(-180.0));
        assertEquals(Integer.MIN_VALUE, SemicircleUtil.degreesToSemicircles32(180.0));
        assertEquals(Integer.MIN_VALUE, SemicircleUtil.radiansToSemicircles32(-Math.PI));
        assertEquals(Integer.MIN_VALUE, SemicircleUtil.radiansToSemicircles32(Math.PI));
        assertEquals(-180.0, SemicircleUtil.semicircles32ToDegrees(Integer.MIN_VALUE));
        assertEquals(-Math.PI, SemicircleUtil.semicircles32ToRadians(Integer.MIN_VALUE));
    }

    @Test
    void keepsAnglesJustInsideHalfTurnBoundsOnTheirOriginalSide() {
        assertTrue(SemicircleUtil.degreesToSemicircles32(Math.nextDown(180.0)) > 0);
        assertTrue(SemicircleUtil.degreesToSemicircles32(Math.nextUp(-180.0)) < 0);
        assertTrue(SemicircleUtil.radiansToSemicircles32(Math.nextDown(Math.PI)) > 0);
        assertTrue(SemicircleUtil.radiansToSemicircles32(Math.nextUp(-Math.PI)) < 0);
    }

    @Test
    void truncatesFractionalIntegerUnitsTowardsZero() {
        for (final double units : new double[] {-1.75, -0.75, 0.75, 1.75}) {
            assertEquals(
                    (int) units,
                    SemicircleUtil.degreesToSemicircles32(units * DEGREES_PER_UNIT_32));
            assertEquals(
                    (int) units,
                    SemicircleUtil.radiansToSemicircles32(units * RADIANS_PER_UNIT_32));
        }
    }

    @Test
    void roundTripsCoordinatesWithinOneIntegerUnit() {
        for (final double degrees :
                new double[] {-179.9999, -122.4194, -47.3769, 0.0, 8.5417, 47.3769, 179.9999}) {
            assertEquals(
                    degrees,
                    SemicircleUtil.semicircles32ToDegrees(
                            SemicircleUtil.degreesToSemicircles32(degrees)),
                    DEGREES_PER_UNIT_32);
            final double radians = Math.toRadians(degrees);
            assertEquals(
                    radians,
                    SemicircleUtil.semicircles32ToRadians(
                            SemicircleUtil.radiansToSemicircles32(radians)),
                    RADIANS_PER_UNIT_32);
        }
    }

    @Test
    void decodesFullIntegerRangeWithoutTreatingReservedValuesSpecially() {
        assertEquals(
                180.0 - DEGREES_PER_UNIT_32,
                SemicircleUtil.semicircles32ToDegrees(Integer.MAX_VALUE));
        assertEquals(
                Math.PI - RADIANS_PER_UNIT_32,
                SemicircleUtil.semicircles32ToRadians(Integer.MAX_VALUE),
                1e-15);
        assertEquals(DEGREES_PER_UNIT_32, SemicircleUtil.semicircles32ToDegrees(1));
        assertEquals(-DEGREES_PER_UNIT_32, SemicircleUtil.semicircles32ToDegrees(-1));
    }

    @Test
    void rejectsNonFiniteAnglesForIntegerEncoding() {
        for (final double angle :
                new double[] {Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SemicircleUtil.degreesToSemicircles32(angle));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SemicircleUtil.radiansToSemicircles32(angle));
        }
    }

    @Test
    void rejectsDegreesOutsideHalfTurnBounds() {
        for (final double degrees :
                new double[] {Math.nextDown(-180.0), Math.nextUp(180.0), -360.0, 360.0}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SemicircleUtil.degreesToSemicircles32(degrees));
        }
    }

    @Test
    void rejectsRadiansOutsideHalfTurnBounds() {
        for (final double radians :
                new double[] {
                    Math.nextDown(-Math.PI), Math.nextUp(Math.PI), -2.0 * Math.PI, 2.0 * Math.PI
                }) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SemicircleUtil.radiansToSemicircles32(radians));
        }
    }
}
