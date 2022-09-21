package ch.ubique.backend.geo.vectortile.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

import java.util.List;

public class GridInfo {

    /** Origin for all zoom levels */
    private final LV95Coordinate origin;

    /**
     * rows of pixel product
     *
     * <p>rows * resolution = width of tile [m]
     */
    private final int rowsPerTile;

    private final List<ZoomLevel> zoomLevels;

    public GridInfo(LV95Coordinate origin, int rowsPerTile, List<ZoomLevel> zoomLevels) {
        this.origin = origin;
        this.rowsPerTile = rowsPerTile;
        this.zoomLevels = zoomLevels;
    }

    public int minLevel() {
        return zoomLevels.stream().mapToInt(ZoomLevel::getLevel).min().getAsInt();
    }

    public int maxLevel() {
        return zoomLevels.stream().mapToInt(ZoomLevel::getLevel).max().getAsInt();
    }

    /** Return given zoom level object, returns Optional.empty() if level does not exist */
    public ZoomLevel getZoomLevel(int level) {
        return zoomLevels.stream()
                .filter(zoomLevel -> zoomLevel.getLevel() == level)
                .findAny()
                .orElse(null);
    }

    /** Width of tile [m] */
    public Double getTileWidth(int level) {
        ZoomLevel zoomLevel = getZoomLevel(level);
        if (zoomLevel == null) {
            return null;
        }
        return getTileWidth(zoomLevel);
    }

    /** Width of tile [m] */
    public Double getTileWidth(ZoomLevel zoomLevel) {
        return zoomLevel.getResolution() * rowsPerTile;
    }

    public LV95Coordinate getOrigin() {
        return origin;
    }
}
