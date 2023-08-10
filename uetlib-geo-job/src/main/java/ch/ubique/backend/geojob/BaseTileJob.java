package ch.ubique.backend.geojob;

import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.TileOutput;
import ch.ubique.backend.job.Job;
import ch.ubique.backend.job.model.JobRunContext;
import ch.ubique.backend.job.model.JobRunState;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public abstract class BaseTileJob implements Job {

    @Override
    public final JobRunState run(final JobRunContext jobRunContext)
            throws InterruptedException, ExecutionException {
        TileGeneratorUtil.generateTiles(outputs());
        return JobRunState.DONE;
    }

    protected abstract Stream<TileOutput> outputs();
}
