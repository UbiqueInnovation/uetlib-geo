package ch.uetlib.geo.tile.common.constants;

import ch.uetlib.geo.coordinate.models.LV95Coordinate;
import ch.uetlib.geo.coordinate.models.WGS84Coordinate;
import ch.uetlib.geo.tile.common.models.TileLevelsDefinition;
import ch.uetlib.geo.tile.common.models.ZoomLevel;

import java.util.List;

public class SwissConstants {

    private SwissConstants() {}

    public static TileLevelsDefinition swissTileDefinition() {
        final var origin = new LV95Coordinate(2420000, 1350000);
        return new TileLevelsDefinition(origin, swisstopoZoomLevels());
    }

    public static List<ZoomLevel> swisstopoZoomLevels() {
        return List.of(
                zoomLevel(0, 1, 1, 4000),
                zoomLevel(1, 1, 1, 3750),
                zoomLevel(2, 1, 1, 3500),
                zoomLevel(3, 1, 1, 3250),
                zoomLevel(4, 1, 1, 3000),
                zoomLevel(5, 1, 1, 2750),
                zoomLevel(6, 1, 1, 2500),
                zoomLevel(7, 1, 1, 2250),
                zoomLevel(8, 1, 1, 2000),
                zoomLevel(9, 2, 1, 1750),
                zoomLevel(10, 2, 1, 1500),
                zoomLevel(11, 2, 1, 1250),
                zoomLevel(12, 2, 2, 1000),
                zoomLevel(13, 3, 2, 750),
                zoomLevel(14, 3, 2, 650),
                zoomLevel(15, 4, 3, 500),
                zoomLevel(16, 8, 5, 250),
                zoomLevel(17, 19, 13, 100),
                zoomLevel(18, 38, 25, 50),
                zoomLevel(19, 94, 63, 20),
                zoomLevel(20, 188, 125, 10),
                zoomLevel(21, 375, 250, 5),
                zoomLevel(22, 750, 500, 2.5),
                zoomLevel(23, 938, 625, 2),
                zoomLevel(24, 1250, 834, 1.5),
                zoomLevel(25, 1875, 1250, 1),
                zoomLevel(26, 3750, 2500, 0.5),
                zoomLevel(27, 7500, 5000, 0.25),
                zoomLevel(28, 18750, 12500, 0.1));
    }

    private static ZoomLevel zoomLevel(
            final int level, final int tilesX, final int tilesY, final double resolution) {
        return new ZoomLevel(level, tilesX, tilesY, resolution * 256, resolution * 256);
    }

    public static WGS84Coordinate getCenterOfCh() {
        return new WGS84Coordinate(46.8012, 8.22669);
    }
}
