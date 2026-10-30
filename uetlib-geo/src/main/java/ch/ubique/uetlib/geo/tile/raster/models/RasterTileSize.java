package ch.ubique.uetlib.geo.tile.raster.models;

/**
 * @param tileWidth the width of the tile in coordinate units
 * @param resolution the number of pixels in each row (typical values are 256 or 512)
 */
public record RasterTileSize(double tileWidth, int resolution) {

    /**
     * @return the width of each pixel on the tile in coordinate units
     */
    public double sizeOfPixel() {
        return tileWidth / resolution;
    }

    /**
     * @return the total number of pixels
     */
    public int numPixel() {
        return resolution * resolution;
    }
}
