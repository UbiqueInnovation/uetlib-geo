package ch.ubique.backend.geo.tile.vector.uploader;

import ch.ubique.backend.core.utils.ProgressLogger;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.storage.StorageClient;
import ch.ubique.backend.storage.output.ByteOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class VectorTileUploader {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileUploader.class);
    private final StorageClient storageClient;

    public VectorTileUploader(final StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    public void upload(final String path, final Collection<BinaryTile> binaryTiles) {
        final var mdcContextMap = MDC.getCopyOfContextMap();
        final var progressLogger =
                new ProgressLogger("VECTOR_TILE_UPLOAD", binaryTiles.size(), mdcContextMap);
        final var watchAndUploadBinaryTile =
                progressLogger.watch((Consumer<BinaryTile>) tile -> uploadBinaryTile(path, tile));

        logger.info("Starting upload. {} tiles in total", binaryTiles.size());
        try (final var forkJoinPool = new ForkJoinPool(12)) {
            forkJoinPool
                    .submit(() -> binaryTiles.parallelStream().forEach(watchAndUploadBinaryTile))
                    .get();
        } catch (final InterruptedException e) {
            logger.error("Exception uploading tiles: ", e);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            logger.error("Exception uploading tiles: ", e);
        }
        logger.info("Upload done");
    }

    private void uploadBinaryTile(final String path, final BinaryTile tile) {
        try {
            upload(path, tile);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void upload(final String path, final BinaryTile tile) throws IOException {
        final var tilePath = Paths.get(path, tile.info().path()).toString();
        final var contentType = "application/protobuf";
        storageClient.write(new ByteOutput(tile.bytes(), contentType), tilePath);
    }
}
