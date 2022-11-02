package ch.ubique.backend.geo.tile.raster.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;

import java.awt.image.RenderedImage;

public record RasterTile(TileInfo tileInfo, RenderedImage data) {
}
