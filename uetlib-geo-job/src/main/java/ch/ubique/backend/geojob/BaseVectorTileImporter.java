package ch.ubique.backend.geojob;

import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.job.BaseInputJob;
import ch.ubique.backend.job.model.JobRunState;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContext;

public abstract class BaseVectorTileImporter<T> extends BaseInputJob<Collection<T>> {

    protected BaseVectorTileImporter(@NotNull final ApplicationContext context) {
        super(context);
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
    protected JobRunState processInputs(Collection<T> data) throws Exception {

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

    @SuppressWarnings("RedundantThrows")
    protected void processAdditionalData() throws Exception {}
}
