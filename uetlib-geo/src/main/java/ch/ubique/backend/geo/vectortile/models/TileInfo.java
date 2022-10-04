package ch.ubique.backend.geo.vectortile.models;

public record TileInfo(int x, int y, int zoomLevel) {

    public String path() {
        return zoomLevel + "/" + x + "/" + y + ".pbf";
    }

    @Override
    public String toString() {
        return "TileInfo{" + "x=" + x + ", y=" + y + ", zoomLevel=" + zoomLevel + '}';
    }
}
