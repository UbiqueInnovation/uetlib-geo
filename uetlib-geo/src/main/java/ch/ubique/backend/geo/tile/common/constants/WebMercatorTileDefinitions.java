package ch.ubique.backend.geo.tile.common.constants;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WEB_MERCATOR;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Envelope;

public class WebMercatorTileDefinitions {

    private static final WebMercatorCoordinate MIN_COORDINATE =
            WEB_MERCATOR.transform(new WGS84Coordinate(-180, -85.06));
    private static final WebMercatorCoordinate MAX_COORDINATE =
            WEB_MERCATOR.transform(new WGS84Coordinate(180, 85.06));
    private static final Envelope BOUNDS =
            new Envelope(MIN_COORDINATE.toJts(), MAX_COORDINATE.toJts());

    private WebMercatorTileDefinitions() {}

    public static TileLevelsDefinition tileDefinitions() {
        final var origin = new WebMercatorCoordinate(MIN_COORDINATE.getX(), MAX_COORDINATE.getY());
        final var maxLevel = 22;
        final var zoomLevels =
                IntStream.range(0, maxLevel + 1)
                        .mapToObj(WebMercatorTileDefinitions::zoomLevel)
                        .toList();
        return new TileLevelsDefinition(origin, zoomLevels);
    }

    @NotNull
    private static ZoomLevel zoomLevel(final int level) {
        final var numTiles = (int) Math.pow(2, level);
        final var tileWidth = BOUNDS.getWidth() / numTiles;
        final var tileHeight = BOUNDS.getHeight() / numTiles;
        return new ZoomLevel(level, numTiles, numTiles, tileWidth, tileHeight);
    }
}
