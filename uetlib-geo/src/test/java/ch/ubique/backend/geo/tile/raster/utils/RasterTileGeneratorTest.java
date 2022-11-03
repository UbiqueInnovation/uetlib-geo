package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;

class RasterTileGeneratorTest {

    @Test
    void test() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().withMaxLevel(24);
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
                return new RasterTile(tileInfo, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
            }
        };

        generator.generateTiles();

        Assertions.assertFalse(queriedSourceTiles.isEmpty());
        Assertions.assertFalse(generatedTiles.isEmpty());
        Assertions.assertEquals(tileLevelsDefinition.numberOfTiles(), generatedTiles.size());
    }

//    @Test
    @Disabled("Only for visualizing locally. ")
    void testImages() {
        final var tileLevelsDefinition = SwissConstants.swissTileDefinition().onlyWithLevels(15);
        final var sourceTileSize = 5000;

        final var generatedTiles = new HashSet<RasterTile>();

        final var outputResolution = 512;

        final var generator = new RasterTileGenerator(tileLevelsDefinition, sourceTileSize, outputResolution) {
            @Override
            protected void onTileGenerated(RasterTile rasterTile) {
                generatedTiles.add(rasterTile);
            }

            @Override
            protected RasterTile getSourceTile(TileInfo tileInfo) {
                final var data = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) data.getGraphics();
                g.setColor(new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
                g.fillRect(0, 0, data.getWidth(), data.getHeight());
                return new RasterTile(tileInfo, data);
            }
        };

        generator.generateTiles();


        final var frame = new JDialog();
        frame.setSize(new Dimension(outputResolution * 3 + 20, outputResolution * 2 + 100));
        frame.setLayout(null);
        for (final var rasterTile : generatedTiles) {
            final var panel = new JPanel();
            final var image = new JLabel(new ImageIcon(rasterTile.data()));
            image.setBounds(0, 0, outputResolution, outputResolution);
            panel.add(image);
            panel.setLayout(null);
            panel.setBounds(rasterTile.tileInfo().x() * outputResolution,  (1 - rasterTile.tileInfo().y()) * outputResolution, outputResolution, outputResolution);
            frame.add(panel);
        }

        frame.setModal(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setVisible(true);

        Assertions.assertTrue(true);
    }
}
