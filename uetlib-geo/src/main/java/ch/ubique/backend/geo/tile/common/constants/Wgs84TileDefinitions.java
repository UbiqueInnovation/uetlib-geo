package ch.ubique.backend.geo.tile.common.constants;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.WGS84;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;

import org.jspecify.annotations.NonNull;

import java.util.stream.IntStream;

public class Wgs84TileDefinitions {

    private Wgs84TileDefinitions() {}

    public static TileLevelsDefinition tileDefinitions() {
        final var origin =
                new WGS84Coordinate(
                        WGS84.maxCoordinate().latitude(), WGS84.minCoordinate().longitude());
        final var maxLevel = 22;
        final var zoomLevels =
                IntStream.range(0, maxLevel + 1).mapToObj(Wgs84TileDefinitions::zoomLevel).toList();
        return new TileLevelsDefinition(origin, zoomLevels);
    }

    @NonNull
    private static ZoomLevel zoomLevel(final int level) {
        final var numTiles = (int) Math.pow(2, level);
        final var tileWidth = WGS84.bounds().getWidth() / numTiles;
        final var tileHeight = WGS84.bounds().getHeight() / numTiles;
        return new ZoomLevel(level, numTiles, numTiles, tileWidth, tileHeight);
    }
}
