package ch.ubique.backend.geo.tile.smartdrawing.utils;

public final class SmartDrawingUtil {

    private SmartDrawingUtil() {}

    public static int booleanArrayToInt(final boolean[] booleans) {
        var n = 0;
        for (int  i = 0; i < booleans.length; i++) {
            if (booleans[i]) {
                n |= 1 << i;
            }
        }
        return n;
    }
}
