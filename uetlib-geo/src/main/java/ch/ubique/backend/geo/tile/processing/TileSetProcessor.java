package ch.ubique.backend.geo.tile.processing;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import ch.ubique.backend.core.utils.MapCollectors;
import ch.ubique.backend.core.utils.ProgressLogger;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.processing.tasks.TileSetTask;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class TileSetProcessor implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService;

    public TileSetProcessor(final int parallelism) {
        final var threadFactory =
                new ThreadFactoryBuilder().setNameFormat("modify-tile-%d").build();
        this.executorService = Executors.newFixedThreadPool(parallelism, threadFactory);
    }

    public <E extends Exception> void executeTask(final TileSetTask<E> task) throws E {
        logger.info("Submitting task.");
        final var taskRunHelper = new TaskRunHelper<>(task);

        final var futures =
                task.selectedTiles().stream()
                        .map(taskRunHelper::createRunnable)
                        .map(runnable -> CompletableFuture.runAsync(runnable, executorService))
                        .toArray(CompletableFuture[]::new);

        logger.info("Created all tasks, waiting for completion.");
        CompletableFuture.allOf(futures).join();
        taskRunHelper.onCompletion();
        taskRunHelper.raiseExceptionIfPresent();
        logger.info("All tasks completed.");
    }

    @Override
    public void close() {
        logger.info("Shutting executor service down.");
        executorService.shutdown();
        logger.info("Executor service terminated.");
    }

    private class TaskRunHelper<E extends Exception> {
        private final Map<Integer, ProgressLogger> progressLoggers;
        private final Map<TileInfo, Exception> thrownExceptions;
        private final TileSetTask<E> task;

        private TaskRunHelper(final TileSetTask<E> task) {
            this.task = task;
            this.thrownExceptions = new ConcurrentHashMap<>();
            final var levelCounts =
                    task.selectedTiles().stream().collect(groupingBy(TileInfo::level, counting()));
            this.progressLoggers =
                    levelCounts.keySet().stream()
                            .collect(
                                    MapCollectors.toMapWithValueMapper(
                                            level ->
                                                    new ProgressLogger(
                                                            "Level " + level + ": ",
                                                            levelCounts.get(level))));
        }

        private Runnable createRunnable(final TileInfo tileInfo) {
            return () -> runForTile(tileInfo);
        }

        private void runForTile(final TileInfo tileInfo) {
            try {
                task.execute(tileInfo);
                progressLoggers.get(tileInfo.level()).step();
            } catch (final Exception e) {
                logger.info("Could not run task for {}: {}", tileInfo, e.getMessage());
                thrownExceptions.put(tileInfo, e);
            }
        }

        private void onCompletion() {
            task.onCompletion();
        }

        private void raiseExceptionIfPresent() throws E {
            if (!thrownExceptions.isEmpty()) {
                logger.info(
                        "{} tiles could not be processed. Rethrowing first exception now.",
                        thrownExceptions.size());
                final var anyException = thrownExceptions.values().iterator().next();

                if (anyException instanceof final RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    //noinspection unchecked
                    throw (E) anyException;
                }
            }
        }
    }
}
