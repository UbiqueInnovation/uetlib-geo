package ch.ubique.backend.geo.vectortile.models;

public class ZoomLevel {

    /**
     * Level identifier
     */
    private int level;

    /**
     * Number of tiles in Y direction
     */
    private int tilesY;


    /**
     * Number of tiles in X direction
     */
    private int tilesX;

    /**
     * Resolution in [m], mainly applicable for pixel data
     * I.e. width of one pixel
     */
    private double resolution;

    public ZoomLevel(int level, int tilesX, int tilesY, double resolution) {
        this.level = level;
        this.tilesY = tilesY;
        this.tilesX = tilesX;
        this.resolution = resolution;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setTilesY(int tilesY) {
        this.tilesY = tilesY;
    }

    public void setTilesX(int tilesX) {
        this.tilesX = tilesX;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public int getLevel() {
        return level;
    }

    public int getTilesY() {
        return tilesY;
    }

    public int getTilesX() {
        return tilesX;
    }

    public double getResolution() {
        return resolution;
    }
}
