package ch.ubique.backend.geo.tile.raster.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;

import java.awt.image.BufferedImage;

public record RasterTile(TileInfo tileInfo, BufferedImage data) {

    public RasterTileSize size() {
        return new RasterTileSize(tileInfo.zoomLevel().tileWidth(), data == null ? -1 : data.getWidth());
    }
}
