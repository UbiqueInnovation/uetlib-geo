package ch.uetlib.geo.tile.processing.tasks;

import ch.uetlib.geo.tile.common.models.TileInfo;
import ch.uetlib.geo.tile.processing.stats.TileSizeStatsUtil;
import ch.uetlib.geo.tile.vector.models.BinaryTile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ModifyTileSetTask extends CreateTileSetTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final TileSizeStatsUtil inputTileSizeUtil = new TileSizeStatsUtil();

    @Override
    protected final BinaryTile createTile(final TileInfo tileInfo) throws IOException {
        final var inputTile = loadTile(tileInfo);
        inputTileSizeUtil.addMeasurement(inputTile);
        return modify(inputTile);
    }

    @Override
    public void onCompletion() throws IOException {
        logger.info("Stats for input");
        inputTileSizeUtil.logStats();

        // Logs stats for output
        super.onCompletion();
    }

    protected abstract BinaryTile loadTile(TileInfo tileInfo) throws IOException;

    protected abstract BinaryTile modify(BinaryTile tile) throws IOException;
}
