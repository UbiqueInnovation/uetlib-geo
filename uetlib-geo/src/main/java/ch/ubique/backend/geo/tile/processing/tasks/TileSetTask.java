package ch.ubique.backend.geo.tile.processing.tasks;

import ch.ubique.backend.geo.tile.common.models.TileInfo;

import java.util.Collection;

public interface TileSetTask<E extends Exception> {

    Collection<TileInfo> selectedTiles();

    void execute(TileInfo tileInfo) throws E;

    default void onCompletion() {
        // nothing here
    }
}
