package ch.ubique.backend.geojob;


import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.job.data.service.InputJobDataService;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Use this vector tile importer when you have only one output and
 * the elements you want to output are returned by {@link InputCollectionLoaderService#loadData()}.
 */
public abstract class SimpleBaseVectorTileImporter extends BaseVectorTileImporter<VectorTileElement> {

    protected SimpleBaseVectorTileImporter(InputJobDataService jobDataService) {
        super(jobDataService);
    }

    protected abstract VectorTileConfig config();

    protected abstract void consumeTile(BinaryTile tile);

    @Override
    protected final Stream<VectorTileOutput> getOutputs(Collection<VectorTileElement> elements) {
        return Stream.of(new VectorTileOutput(config(), elements, this::consumeTile));
    }
}
