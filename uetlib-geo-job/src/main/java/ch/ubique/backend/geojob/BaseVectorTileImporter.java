package ch.ubique.backend.geojob;


import ch.ubique.backend.core.file.ByteFile;
import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileGenerator;
import ch.ubique.backend.job.BaseInputJob;
import ch.ubique.backend.job.data.service.InputJobDataService;
import ch.ubique.backend.job.model.Job;
import ch.ubique.backend.job.model.JobRun;
import ch.ubique.backend.job.model.JobRunInput;
import ch.ubique.backend.job.model.JobRunState;
import ch.ubique.backend.job.model.entity.JobRunInputEntity;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public abstract class BaseVectorTileImporter<T> extends BaseInputJob {


    private DataAndInput<Collection<T>> data;

    protected BaseVectorTileImporter(InputJobDataService jobDataService) {
        super(jobDataService);
    }

    protected abstract InputCollectionLoaderService<T> getService();

    protected abstract Stream<VectorTileOutput> getOutputs(Collection<T> elements);

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
        logger.info("Getting outputs.");
        final var outputs = getOutputs(data.data());
        final var iterator = outputs.iterator();

        logger.info("Processing each output.");
        while (iterator.hasNext()) {

            // Get output
            final var output = iterator.next();
            logger.info("Got output {}.", output);

            // Validate output
            if (output.isEmpty()) {
                if (allowEmpty()) {
                    logger.info("No elements found. Continuing to generate empty vector tiles.");
                } else {
                    logger.error("No elements found. Aborting.");
                    return JobRunState.ERROR;
                }
            }

            // Generate vector tiles
            try {
                logger.info("Generating and uploading tiles");
                VectorTileGenerator.generateForSwitzerlandLv95(output);
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
