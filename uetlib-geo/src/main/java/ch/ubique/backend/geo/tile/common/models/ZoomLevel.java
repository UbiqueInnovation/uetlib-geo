package ch.ubique.backend.geo.tile.common.models;

/**
 * @param level     Level identifier
 * @param tilesY    Number of tiles in Y direction
 * @param tilesX    Number of tiles in X direction
 * @param tileWidth Width of one tile in coordinate system units
 *                  (for swiss coordinate systems LV03/LV95 this would be meters [m])
 */
public record ZoomLevel(
        int level,
        int tilesX,
        int tilesY,
        double tileWidth
) {
    public static ZoomLevel unbounded(int level, double tileWidth) {
        return new ZoomLevel(level, Integer.MAX_VALUE, Integer.MAX_VALUE, tileWidth);
    }

    @Override
    public String toString() {
        if (level == Integer.MAX_VALUE) {
            return "max";
        }
        return String.valueOf(level);
    }

    public long numberOfTiles() {
        return (long) tilesX * tilesY;
    }
}
