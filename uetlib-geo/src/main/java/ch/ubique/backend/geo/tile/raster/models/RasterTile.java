package ch.ubique.backend.geo.tile.raster.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public record RasterTile(TileInfo tileInfo, BufferedImage data) {

    public static RasterTile empty(TileInfo tileInfo) {
        return new RasterTile(tileInfo, null);
    }

    public boolean isEmpty() {
        return data == null;
    }

    public RasterTileSize size() {
        return new RasterTileSize(tileInfo.zoomLevel().tileWidth(), data == null ? -1 : data.getWidth());
    }

    public Optional<byte[]> toPng() {
        return toImage("png");
    }

    public Optional<byte[]> toJpg() {
        return toImage("jpg");
    }

    public Optional<byte[]> toImage(String formatName) {
        if (isEmpty()) {
            return Optional.empty();
        }
        try (final var outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(data, formatName, outputStream);
            return Optional.of(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Writing to a ByteArrayOutputStream should not throw an IOException.", e);
        }
    }
}
