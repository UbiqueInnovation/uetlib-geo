package ch.ubique.backend.geo.tile.smartdrawing.utils;

public final class SmartDrawingUtil {

    private SmartDrawingUtil() {}

    public static int booleanArrayToInt(final boolean[] booleans) {
        var n = 0;
        for (final var b : booleans) {
            n = (n << 1) + (b ? 1 : 0);
        }
        return n;
    }
}
