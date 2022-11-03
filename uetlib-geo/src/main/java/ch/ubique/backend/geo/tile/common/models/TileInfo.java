package ch.ubique.backend.geo.tile.common.models;

public record TileInfo(int x, int y, ZoomLevel zoomLevel) {

    public String path() {
        return level() + "/" + x + "/" + y + ".pbf";
    }

    public int level() {
        return zoomLevel.level();
    }

    @Override
    public String toString() {
        return zoomLevel.toString() + "_" + x + "_" + y;
    }
}
