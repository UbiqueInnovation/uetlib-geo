package ch.ubique.uetlib.geo.tile.raster.utils;

import ch.ubique.uetlib.geo.tile.common.constants.SwissConstants;
import ch.ubique.uetlib.geo.tile.common.models.TileInfo;
import ch.ubique.uetlib.geo.tile.raster.models.RasterTile;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("For local testing only")
class RasterTileGeneratorTest {

    private static final Random random = new Random();

    @Test
    void test() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().withMaxLevel(17);
        final var sourceTileSize = 1000;

        final var generatedTiles = new HashSet<TileInfo>();
        final var queriedSourceTiles = new HashSet<TileInfo>();

        final var generator =
                new RasterTileGenerator(tileLevelsDefinition, sourceTileSize, sourceTileSize, 256) {
                    @Override
                    protected @NonNull RasterTile getSourceTile(final @NonNull TileInfo tileInfo) {
                        Assertions.assertFalse(queriedSourceTiles.contains(tileInfo));
                        queriedSourceTiles.add(tileInfo);
                        return tileWithRandomColor(tileInfo);
                    }

                    @Override
                    protected void onTileGenerated(final RasterTile rasterTile) {
                        Assertions.assertFalse(generatedTiles.contains(rasterTile.tileInfo()));
                        generatedTiles.add(rasterTile.tileInfo());
                        writeToFile(rasterTile);
                    }
                };

        generator.generateTiles();

        Assertions.assertFalse(queriedSourceTiles.isEmpty());
        Assertions.assertFalse(generatedTiles.isEmpty());
        Assertions.assertEquals(tileLevelsDefinition.numberOfTiles(), generatedTiles.size());
    }

    @Test
    @Disabled("Only for visualizing locally. ")
    void testImages() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().onlyWithLevels(12);
        final var sourceTileSize = 400 * 1000;

        final var generatedTiles = new HashSet<RasterTile>();

        final var outputResolution = 512;

        final var generator =
                new RasterTileGenerator(
                        tileLevelsDefinition, sourceTileSize, sourceTileSize, outputResolution) {
                    @Override
                    protected @NonNull RasterTile getSourceTile(final @NonNull TileInfo tileInfo) {
                        return tileWithRandomColor(tileInfo);
                    }

                    @Override
                    protected void onTileGenerated(final @NonNull RasterTile rasterTile) {
                        generatedTiles.add(rasterTile);
                    }
                };

        generator.generateTiles();

        final var frame = new JDialog();
        frame.setSize(new Dimension(outputResolution * 3 + 20, outputResolution * 2 + 100));
        frame.setLayout(null);
        for (final var rasterTile : generatedTiles) {

            if (tileLevelsDefinition.isMin(rasterTile.tileInfo().zoomLevel())) {
                final var panel = new JPanel();
                final var image = new JLabel(new ImageIcon(rasterTile.data()));
                image.setBounds(0, 0, outputResolution, outputResolution);
                panel.add(image);
                panel.setLayout(null);
                panel.setBounds(
                        rasterTile.tileInfo().x() * outputResolution,
                        rasterTile.tileInfo().y() * outputResolution,
                        outputResolution,
                        outputResolution);
                frame.add(panel);
            }

            writeToFile(rasterTile);
        }

        frame.setModal(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setVisible(true);

        Assertions.assertTrue(true);
    }

    private void writeToFile(final RasterTile rasterTile) {
        try {
            final var output =
                    new File(
                            "/tmp/"
                                    + getClass().getName()
                                    + '/'
                                    + rasterTile.tileInfo().path()
                                    + ".png");
            output.mkdirs();
            ImageIO.write(rasterTile.data(), "png", output);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RasterTile tileWithRandomColor(final TileInfo tileInfo) {
        final var data = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = (Graphics2D) data.getGraphics();
        g.setPaint(
                new GradientPaint(
                        new Point(0, 0),
                        randomColor(),
                        new Point(data.getHeight(), data.getWidth()),
                        Color.black));
        g.fillRect(0, 0, data.getWidth(), data.getHeight());
        g.dispose();
        return new RasterTile(tileInfo, data);
    }

    private Color randomColor() {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }
}
