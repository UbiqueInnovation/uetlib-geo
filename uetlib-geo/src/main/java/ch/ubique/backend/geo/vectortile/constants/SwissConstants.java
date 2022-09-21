package ch.ubique.backend.geo.vectortile.constants;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.vectortile.models.GridInfo;
import ch.ubique.backend.geo.vectortile.models.ZoomLevel;

import java.util.ArrayList;
import java.util.List;

public class SwissConstants {

    private SwissConstants() {}

    public static GridInfo swissGridInfo() {
        LV95Coordinate origin = new LV95Coordinate(2420000, 1350000);
        return new GridInfo(origin, 256, swisstopoZoomLevels());
    }

    public static List<ZoomLevel> swisstopoZoomLevels() {
        List<ZoomLevel> zoomLevels = new ArrayList<>();
        zoomLevels.add(new ZoomLevel(0, 1, 1, 4000));
        zoomLevels.add(new ZoomLevel(1, 1, 1, 3750));
        zoomLevels.add(new ZoomLevel(2, 1, 1, 3500));
        zoomLevels.add(new ZoomLevel(3, 1, 1, 3250));
        zoomLevels.add(new ZoomLevel(4, 1, 1, 3000));
        zoomLevels.add(new ZoomLevel(5, 1, 1, 2750));
        zoomLevels.add(new ZoomLevel(6, 1, 1, 2500));
        zoomLevels.add(new ZoomLevel(7, 1, 1, 2250));
        zoomLevels.add(new ZoomLevel(8, 1, 1, 2000));
        zoomLevels.add(new ZoomLevel(9, 2, 1, 1750));
        zoomLevels.add(new ZoomLevel(10, 2, 1, 1500));
        zoomLevels.add(new ZoomLevel(11, 2, 1, 1250));
        zoomLevels.add(new ZoomLevel(12, 2, 2, 1000));
        zoomLevels.add(new ZoomLevel(13, 3, 2, 750));
        zoomLevels.add(new ZoomLevel(14, 3, 2, 650));
        zoomLevels.add(new ZoomLevel(15, 4, 3, 500));
        zoomLevels.add(new ZoomLevel(16, 8, 5, 250));
        zoomLevels.add(new ZoomLevel(17, 19, 13, 100));
        zoomLevels.add(new ZoomLevel(18, 38, 25, 50));
        zoomLevels.add(new ZoomLevel(19, 94, 63, 20));
        zoomLevels.add(new ZoomLevel(20, 188, 125, 10));
        zoomLevels.add(new ZoomLevel(21, 375, 250, 5));
        zoomLevels.add(new ZoomLevel(22, 750, 500, 2.5));
        zoomLevels.add(new ZoomLevel(23, 938, 625, 2));
        zoomLevels.add(new ZoomLevel(24, 1250, 834, 1.5));
        zoomLevels.add(new ZoomLevel(25, 1875, 1250, 1));
        zoomLevels.add(new ZoomLevel(26, 3750, 2500, 0.5));
        zoomLevels.add(new ZoomLevel(27, 7500, 5000, 0.25));
        zoomLevels.add(new ZoomLevel(28, 18750, 12500, 0.1));
        return zoomLevels;
    }
}
