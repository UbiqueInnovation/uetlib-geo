package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.locationtech.jts.awt.ShapeWriter;

public final class PngVectorTileEncoder extends AbstractVectorTileEncoder {

    private final VectorTileConfig config;

    public PngVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public byte[] encodeVectorTile(final VectorTile tile) {
        final int extent = config.getExtent();
        final var image = new BufferedImage(extent, extent, BufferedImage.TYPE_INT_ARGB);
        final var shapeWriter = new ShapeWriter();
        final var graphics = image.createGraphics();
        graphics.setColor(new Color(155, 42, 211));

        tile.elements().stream()
                .map(e -> getTileGeometry(e.geometry(), tile.tileInfo().envelope()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(shapeWriter::toShape)
                .forEach(graphics::draw);

        try (final var outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
