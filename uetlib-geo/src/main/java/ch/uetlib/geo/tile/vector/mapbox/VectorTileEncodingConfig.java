package ch.uetlib.geo.tile.vector.mapbox;

public final class VectorTileEncodingConfig {

    private VectorTileEncodingConfig() {}

    /** This constant can be set to false in any project to disable unsigned numbers */
    @SuppressWarnings("java:S1444") // Should not be final, as projects might want to set this
    public static boolean allowUnsignedNumbers = true;
}
