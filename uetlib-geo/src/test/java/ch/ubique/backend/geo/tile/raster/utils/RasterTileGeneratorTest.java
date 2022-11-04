package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

@Disabled("For local testing only")
class RasterTileGeneratorTest {

//    @Test
    void test() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().withMaxLevel(23);
        final var sourceTileSize = 1000;

        final var generatedTiles = new HashSet<TileInfo>();
        final var queriedSourceTiles = new HashSet<TileInfo>();

        final var generator = new RasterTileGenerator(tileLevelsDefinition, sourceTileSize, 256) {
            @Override
            protected void onTileGenerated(RasterTile rasterTile) {
                Assertions.assertFalse(generatedTiles.contains(rasterTile.tileInfo()));
                generatedTiles.add(rasterTile.tileInfo());
            }

            @Override
            protected RasterTile getSourceTile(TileInfo tileInfo) {
                Assertions.assertFalse(queriedSourceTiles.contains(tileInfo));
                queriedSourceTiles.add(tileInfo);
                return tileWithRandomColor(tileInfo);
            }
        };

        generator.generateTiles();

        Assertions.assertFalse(queriedSourceTiles.isEmpty());
        Assertions.assertFalse(generatedTiles.isEmpty());
        Assertions.assertEquals(tileLevelsDefinition.numberOfTiles(), generatedTiles.size());
    }

//    @Test
    @Disabled("Only for visualizing locally. ")
    void testImages() throws IOException {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().withMinMaxLevel(8, 20);
        final var sourceTileSize = 100 * 1000;

        final var generatedTiles = new HashSet<RasterTile>();

        final var outputResolution = 512;

        final var generator = new RasterTileGenerator(tileLevelsDefinition, sourceTileSize, outputResolution) {
            @Override
            protected void onTileGenerated(RasterTile rasterTile) {
                generatedTiles.add(rasterTile);
            }

            @Override
            protected RasterTile getSourceTile(TileInfo tileInfo) {
                return tileWithRandomColor(tileInfo);
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
                panel.setBounds(rasterTile.tileInfo().x() * outputResolution,  rasterTile.tileInfo().y() * outputResolution, outputResolution, outputResolution);
                frame.add(panel);
            }

            try {
                final var output = new File("/tmp/" + getClass().getName() + '/' + rasterTile.tileInfo().path() + ".png");
                output.mkdirs();
                ImageIO.write(rasterTile.data(), "png", output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        frame.setModal(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setVisible(true);

        Assertions.assertTrue(true);
    }

    private RasterTile tileWithRandomColor(TileInfo tileInfo) {
        final var data = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) data.getGraphics();
        g.setPaint(new GradientPaint(new Point(0, 0), randomColor(), new Point(data.getHeight(), data.getWidth()), Color.black));
        g.fillRect(0, 0, data.getWidth(), data.getHeight());
        g.dispose();
        return new RasterTile(tileInfo, data);
    }

    private Color randomColor() {
        return new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
    }
}
