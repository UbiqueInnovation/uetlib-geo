package ch.uetlib.geo.tile.processing.tasks;

import ch.uetlib.geo.tile.common.models.TileInfo;
import ch.uetlib.geo.tile.processing.stats.TileSizeStatsUtil;
import ch.uetlib.geo.tile.vector.models.BinaryTile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class CreateTileSetTask implements TileSetTask<IOException> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TileSizeStatsUtil tileSizeStatsUtil = new TileSizeStatsUtil();

    @Override
    public final void execute(final TileInfo tileInfo) throws IOException {
        final var tile = createTile(tileInfo);
        tileSizeStatsUtil.addMeasurement(tile);
        saveTile(tile);
    }

    @Override
    public void onCompletion() throws IOException {
        logger.info("Stats for output");
        tileSizeStatsUtil.logStats();
    }

    protected abstract BinaryTile createTile(TileInfo tile) throws IOException;

    protected abstract void saveTile(BinaryTile tile) throws IOException;
}
