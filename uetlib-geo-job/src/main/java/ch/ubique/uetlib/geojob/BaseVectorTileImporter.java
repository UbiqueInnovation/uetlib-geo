package ch.ubique.uetlib.geojob;

import ch.ubique.uetlib.core.service.DataAndInput;
import ch.ubique.uetlib.core.service.InputCollectionLoaderService;
import ch.ubique.uetlib.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.uetlib.job.BaseInputJob;
import ch.ubique.uetlib.job.model.JobRunState;
import java.util.Collection;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;

public abstract class BaseVectorTileImporter<T> extends BaseInputJob<Collection<T>> {

    protected BaseVectorTileImporter(@NonNull final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    protected abstract InputCollectionLoaderService<T> getService();

    protected abstract Stream<VectorTileOutput> getOutputs(Collection<T> elements);

    protected boolean allowEmpty() {
        return false;
    }

    @Override
    protected final DataAndInput<Collection<T>> loadInputs() throws Exception {
        return getService().loadData();
    }

    @Override
    protected JobRunState processInputs(final Collection<T> data) throws Exception {

        if (data.isEmpty()) {
            if (allowEmpty()) {
                logger.info("No elements found. Continuing to generate empty vector tiles.");
            } else {
                logger.error("No elements found. Aborting.");
                return JobRunState.ERROR;
            }
        }

        processAdditionalData();

        logger.info("Getting outputs.");
        final var outputs = getOutputs(data);
        TileGeneratorUtil.generateTiles(outputs);

        return JobRunState.DONE;
    }

    @SuppressWarnings({"RedundantThrows", "java:S112"})
    protected void processAdditionalData() throws Exception {}
}
