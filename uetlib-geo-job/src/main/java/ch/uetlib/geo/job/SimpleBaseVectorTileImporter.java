package ch.uetlib.geo.job;

import ch.uetlib.core.service.InputCollectionLoaderService;
import ch.uetlib.geo.tile.vector.models.BinaryTile;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;
import ch.uetlib.geo.tile.vector.models.VectorTileElement;
import ch.uetlib.geo.tile.vector.models.VectorTileOutput;

import org.jspecify.annotations.NonNull;
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

    protected SimpleBaseVectorTileImporter(@NonNull final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    protected abstract VectorTileConfig config();

    protected abstract void consumeTile(BinaryTile tile) throws IOException;

    @Override
    protected final Stream<VectorTileOutput> getOutputs(
            final Collection<VectorTileElement> elements) {
        return Stream.of(new VectorTileOutput(config(), elements, this::consumeTile));
    }
}
