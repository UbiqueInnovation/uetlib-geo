package ch.ubique.backend.geojob;

import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.TileOutput;
import ch.ubique.backend.job.BaseJob;
import ch.ubique.backend.job.data.service.InputJobDataService;
import ch.ubique.backend.job.model.JobRunState;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public abstract class BaseTileJob extends BaseJob {

    protected BaseTileJob(InputJobDataService jobDataService) {
        super(jobDataService);
    }

    @Override
    protected final JobRunState internalRun() throws InterruptedException, ExecutionException {
        TileGeneratorUtil.generateTiles(outputs());
        return JobRunState.DONE;
    }

    protected abstract Stream<TileOutput> outputs();
}
