package ch.uetlib.geo.job;

import ch.uetlib.geo.tile.common.utils.TileGeneratorUtil;
import ch.uetlib.geo.tile.vector.models.TileOutput;
import ch.uetlib.job.Job;
import ch.uetlib.job.model.JobRunContext;
import ch.uetlib.job.model.JobRunState;

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
