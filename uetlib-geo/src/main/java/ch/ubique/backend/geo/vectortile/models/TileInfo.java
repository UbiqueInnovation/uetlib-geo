package ch.ubique.backend.geo.vectortile.models;

public class TileInfo {

    private final int x;
    private final int y;
    private final int zoomLevel;

    public TileInfo(int x, int y, int zoomLevel) {
        this.x = x;
        this.y = y;
        this.zoomLevel = zoomLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    @Override
    public String toString() {
        return "TileInfo{" + "x=" + x + ", y=" + y + ", zoomLevel=" + zoomLevel + '}';
    }
}
