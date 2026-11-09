package ch.uetlib.geo.tile.processing.tasks;

import ch.uetlib.geo.tile.common.models.TileInfo;

import java.util.Collection;

public interface TileSetTask<E extends Exception> {

    Collection<TileInfo> selectedTiles();

    void execute(TileInfo tileInfo) throws E;

    default void preprocess() throws E {
        // nothing here
    }

    default void onCompletion() throws E {
        // nothing here
    }
}
