package ch.ubique.backend.geojob;

import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.TileOutput;
import ch.ubique.backend.job.Job;
import ch.ubique.backend.job.model.JobRunState;

import jakarta.validation.constraints.NotNull;

import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public abstract class BaseTileJob extends Job {

    protected BaseTileJob(@NotNull final ApplicationContext context) {
        super(context);
    }

    @Override
    protected final JobRunState internalRun() throws InterruptedException, ExecutionException {
        TileGeneratorUtil.generateTiles(outputs());
        return JobRunState.DONE;
    }

    protected abstract Stream<TileOutput> outputs();
}
