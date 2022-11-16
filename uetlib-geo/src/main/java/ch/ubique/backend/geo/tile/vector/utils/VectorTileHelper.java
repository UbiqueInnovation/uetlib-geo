package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import com.pivovarit.function.ThrowingConsumer;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * @deprecated Please use {@linkplain  VectorTileGenerator#generateForSwitzerlandLv95(VectorTileOutput)} instead
 */
@Deprecated(forRemoval = true, since = "0.7.22")
public class VectorTileHelper<T extends VectorTileElement> {

    private final VectorTileConfig<T> config;
    private final Collection<T> elements;

    public VectorTileHelper(Collection<T> elements, VectorTileConfig<T> config) {
        this.config = config;
        this.elements = elements;
    }

    public void generateTiles(
            ThrowingConsumer<BinaryTile, IOException> tileConsumer, int numThreads)
            throws ExecutionException, InterruptedException {
        VectorTileGenerator.generateForSwitzerlandLv95(new VectorTileOutput<>(config, elements, tileConsumer));
    }

    public Collection<BinaryTile> generateTiles() throws ExecutionException, InterruptedException {
        final Collection<BinaryTile> binaryTiles = new ConcurrentLinkedQueue<>();
        generateTiles(binaryTiles::add, 4);
        return binaryTiles;
    }

    public Collection<BinaryTile> generateTiles(int numThreads)
            throws ExecutionException, InterruptedException {
        final Collection<BinaryTile> binaryTiles = new ConcurrentLinkedQueue<>();
        generateTiles(binaryTiles::add, numThreads);
        return binaryTiles;
    }
}
