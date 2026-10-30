package ch.ubique.uetlib.geo.tile.common.constants;

import static ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystems.WEB_MERCATOR;

import ch.ubique.uetlib.geo.coordinate.models.WebMercatorCoordinate;
import ch.ubique.uetlib.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.uetlib.geo.tile.common.models.ZoomLevel;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;

public class WebMercatorTileDefinitions {

    private WebMercatorTileDefinitions() {}

    public static TileLevelsDefinition tileDefinitions() {
        final var origin =
                new WebMercatorCoordinate(
                        WEB_MERCATOR.minCoordinate().x(), WEB_MERCATOR.maxCoordinate().y());
        final var maxLevel = 22;
        final var zoomLevels =
                IntStream.range(0, maxLevel + 1)
                        .mapToObj(WebMercatorTileDefinitions::zoomLevel)
                        .toList();
        return new TileLevelsDefinition(origin, zoomLevels);
    }

    @NonNull
    private static ZoomLevel zoomLevel(final int level) {
        final var numTiles = (int) Math.pow(2, level);
        final var tileWidth = WEB_MERCATOR.bounds().getWidth() / numTiles;
        final var tileHeight = WEB_MERCATOR.bounds().getHeight() / numTiles;
        return new ZoomLevel(level, numTiles, numTiles, tileWidth, tileHeight);
    }
}
