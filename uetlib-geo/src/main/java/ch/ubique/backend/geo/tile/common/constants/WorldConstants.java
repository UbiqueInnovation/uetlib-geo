package ch.ubique.backend.geo.tile.common.constants;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class WorldConstants {

    private WorldConstants() {}

    public static TileLevelsDefinition tileDefinitions() {
        final var origin = new WGS84Coordinate(90, -180);
        final var maxLevel = 22;
        return new TileLevelsDefinition(
                origin,
                IntStream.range(0, maxLevel + 1).mapToObj(WorldConstants::zoomLevel).toList(),
                origin.srid());
    }

    @NotNull
    private static ZoomLevel zoomLevel(final int level) {
        final var numTiles = (int) Math.pow(2, level);
        final var tileHeight = 180. / numTiles;
        final var tileWidth = 360. / numTiles;
        return new ZoomLevel(level, numTiles, numTiles, tileWidth, tileHeight);
    }
}
