package ch.ubique.uetlib.geo.job;

import ch.ubique.uetlib.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.uetlib.geo.tile.vector.models.TileOutput;
import ch.ubique.uetlib.job.Job;
import ch.ubique.uetlib.job.model.JobRunContext;
import ch.ubique.uetlib.job.model.JobRunState;

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
