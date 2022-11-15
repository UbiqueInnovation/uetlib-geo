package ch.ubique.backend.geojob;


import ch.ubique.backend.core.file.ByteFile;
import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileHelper;
import ch.ubique.backend.job.BaseInputJob;
import ch.ubique.backend.job.data.service.InputJobDataService;
import ch.ubique.backend.job.model.Job;
import ch.ubique.backend.job.model.JobRun;
import ch.ubique.backend.job.model.JobRunInput;
import ch.ubique.backend.job.model.JobRunState;
import ch.ubique.backend.job.model.entity.JobRunInputEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class BaseVectorTileImporter<S, T extends VectorTileElement> extends BaseInputJob {


    private DataAndInput<Collection<S>> data;

    protected BaseVectorTileImporter(InputJobDataService jobDataService) {
        super(jobDataService);
    }

    protected abstract InputCollectionLoaderService<S> getService();

    protected abstract Collection<VectorTileOutput<T>> getOutputs(Collection<S> elements);

    protected boolean isInputFileUploadEnabled() {
        return true;
    }

    protected boolean allowEmpty() {
        return false;
    }


    @Override
    protected final Collection<JobRunInput> loadInputs() throws Exception {
        data = getService().loadData();
        uploadInputFiles(data.inputFiles());
        return saveInputFilesToDb(data.inputFiles());
    }

    @Override
    protected final JobRunState processInputs() {
        final var outputs = getOutputs(data.data());
        for (final var output : outputs) {

            if (output.isEmpty()) {
                if (allowEmpty()) {
                    logger.info("No elements found. Continuing to generate empty vector tiles.");
                } else {
                    logger.error("No elements found. Aborting.");
                    return JobRunState.ERROR;
                }
            }

            logger.info("Generating and uploading tiles");
            var vectorTileHelper = new VectorTileHelper<>(output.elements(), output.config());

            try {
                vectorTileHelper.generateTiles(output.tileConsumer(), 32);
            } catch (ExecutionException e) {
                logger.error("Generating vector tiles failed", e);
                return JobRunState.ERROR;
            } catch (InterruptedException e) {
                logger.warn("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        return JobRunState.DONE;
    }

    @NotNull
    private List<JobRunInput> saveInputFilesToDb(Collection<ByteFile> inputFiles) {
        return inputFiles.stream()
                .map(
                        file -> {
                            final var input = new JobRunInputEntity();
                            input.setRunId(jobRun.getId());
                            input.setName(file.getFileName());
                            input.setHash(file.getFileHash());
                            return input;
                        })
                .map(JobRunInput.class::cast)
                .toList();
    }

    private void uploadInputFiles(Collection<ByteFile> inputFiles) throws IOException {
        if (!isInputFileUploadEnabled()) {
            return;
        }

        for (var file : inputFiles) {
            logger.info("Saving job run input file {}.", file.getFileName());
            saveJobRunInput(file, job, jobRun);
        }
    }

    protected abstract void saveJobRunInput(ByteFile file, Job job, JobRun jobRun) throws IOException;
}
