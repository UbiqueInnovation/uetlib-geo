package ch.ubique.backend.geo.tile.processing.tasks;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.processing.stats.TileSizeStatsUtil;

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
    public void onCompletion() {
        logger.info("Stats for output");
        tileSizeStatsUtil.logStats();
    }

    protected abstract BinaryTile createTile(TileInfo tile) throws IOException;

    protected abstract void saveTile(BinaryTile tile) throws IOException;
}
