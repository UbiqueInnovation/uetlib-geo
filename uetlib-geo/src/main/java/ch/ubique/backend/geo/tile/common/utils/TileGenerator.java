package ch.ubique.backend.geo.tile.common.utils;

import java.util.concurrent.ExecutionException;

public interface TileGenerator {

    void generateTiles() throws ExecutionException, InterruptedException;
}
