package ch.ubique.backend.geo.tile.common.models;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @param level Level identifier
 * @param tilesY Number of tiles in Y direction
 * @param tilesX Number of tiles in X direction
 * @param tileWidth Width of one tile in coordinate system units (for swiss coordinate systems
 *     LV03/LV95 this would be meters [m], for WGS 84, this would be degrees)
 * @param tileHeight Width of one tile in coordinate system units (for swiss coordinate systems
 *     LV03/LV95 this would be meters [m], for WGS 84, this would be degrees)
 */
public record ZoomLevel(int level, int tilesX, int tilesY, double tileWidth, double tileHeight) {
    public static ZoomLevel unbounded(int level, double tileWidth, double tileHeight) {
        return new ZoomLevel(level, Integer.MAX_VALUE, Integer.MAX_VALUE, tileWidth, tileHeight);
    }

    public long numberOfTiles() {
        return (long) tilesX * tilesY;
    }

    public Stream<TileInfo> tileInfos() {
        return IntStream.range(0, tilesX)
                .boxed()
                .flatMap(x -> IntStream.range(0, tilesY).mapToObj(y -> new TileInfo(x, y, this)));
    }

    @Override
    public String toString() {
        if (level == Integer.MAX_VALUE) {
            return "max";
        }
        return String.valueOf(level);
    }
}
