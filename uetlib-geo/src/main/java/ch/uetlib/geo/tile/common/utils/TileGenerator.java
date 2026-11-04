package ch.uetlib.geo.tile.common.utils;

import java.util.concurrent.ExecutionException;

public interface TileGenerator {

    void generateTiles() throws ExecutionException, InterruptedException;
}
