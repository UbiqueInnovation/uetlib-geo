package ch.uetlib.geo.tile.vector.encoder;

import ch.uetlib.geo.tile.vector.models.VectorTile;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;

import org.locationtech.jts.awt.ShapeWriter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import javax.imageio.ImageIO;

public final class PngVectorTileEncoder extends AbstractVectorTileEncoder {

    private final VectorTileConfig config;

    public PngVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public byte[] encodeVectorTile(final VectorTile tile) {
        final var extent = config.extent();
        final var image = new BufferedImage(extent, extent, BufferedImage.TYPE_INT_ARGB);
        final var shapeWriter = new ShapeWriter();
        final var graphics = image.createGraphics();
        graphics.setColor(new Color(155, 42, 211));

        tile.elements().stream()
                .map(e -> getTileGeometry(e, tile.tileInfo().envelope()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(shapeWriter::toShape)
                .forEach(graphics::draw);

        try (final var outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
