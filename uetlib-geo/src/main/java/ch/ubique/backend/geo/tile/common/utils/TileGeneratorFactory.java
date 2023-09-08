package ch.ubique.backend.geo.tile.common.utils;

import ch.ubique.backend.geo.tile.vector.models.TileOutput;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileGenerator;

import org.apache.commons.lang3.NotImplementedException;

public final class TileGeneratorFactory {

    private TileGeneratorFactory() {}

    public static TileGenerator getGenerator(final TileOutput output) {
        if (output instanceof final VectorTileOutput vectorTileOutput) {
            final var vectorTileGeneratorParallelism =
                    vectorTileOutput.config().vectorTileGeneratorParallelism();
            return new VectorTileGenerator(vectorTileOutput, vectorTileGeneratorParallelism);
        }

        throw new NotImplementedException(
                "No generator implemented for " + output.getClass() + " yet.");
    }
}
