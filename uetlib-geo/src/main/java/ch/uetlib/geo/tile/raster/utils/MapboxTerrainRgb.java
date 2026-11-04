package ch.uetlib.geo.tile.raster.utils;

public class MapboxTerrainRgb {

    private MapboxTerrainRgb() {}

    public static float decode(final int[] rgb) {
        return decode(rgb[0], rgb[1], rgb[2]);
    }

    public static float decode(final int r, final int g, final int b) {
        return -10000 + decodeValue(r, g, b) * 0.1f;
    }

    public static int[] encode(final float elevation) {
        final var value = Math.round((elevation * 10 + 100000));
        return encodeValue(value);
    }

    public static int decodeValue(final int[] rgb) {
        return decodeValue(rgb[0], rgb[1], rgb[2]);
    }

    public static int decodeValue(final int r, final int g, final int b) {
        return r * 256 * 256 + g * 256 + b;
    }

    public static int[] encodeValue(final int value) {
        final var r = (value >> 16) % 256;
        final var g = (value >> 8) % 256;
        final var b = value % 256;
        return new int[] {r, g, b};
    }
}
