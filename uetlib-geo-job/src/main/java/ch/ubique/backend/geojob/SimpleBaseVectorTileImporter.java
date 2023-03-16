package ch.ubique.backend.geojob;

import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;

import jakarta.validation.constraints.NotNull;

import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Use this vector tile importer when you have only one output and the elements you want to output
 * are returned by {@link InputCollectionLoaderService#loadData()}.
 */
public abstract class SimpleBaseVectorTileImporter
        extends BaseVectorTileImporter<VectorTileElement> {

    protected SimpleBaseVectorTileImporter(@NotNull final ApplicationContext context) {
        super(context);
    }

    protected abstract VectorTileConfig config();

    protected abstract void consumeTile(BinaryTile tile) throws IOException;

    @Override
    protected final Stream<VectorTileOutput> getOutputs(Collection<VectorTileElement> elements) {
        return Stream.of(new VectorTileOutput(config(), elements, this::consumeTile));
    }
}
