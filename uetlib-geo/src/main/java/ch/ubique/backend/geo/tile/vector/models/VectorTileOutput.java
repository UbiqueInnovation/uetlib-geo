package ch.ubique.backend.geo.tile.vector.models;

import com.pivovarit.function.ThrowingConsumer;

import java.io.IOException;
import java.util.Collection;

public record VectorTileOutput<T extends VectorTileElement>(
        VectorTileConfig<T> config,
        Collection<T> elements,
        ThrowingConsumer<BinaryTile, IOException> tileConsumer
) {
    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }
}
