package ch.ubique.backend.geo.tile.vector.uploader;

import static java.lang.Math.max;

import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.storage.StorageClient;
import ch.ubique.backend.storage.output.ByteOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class VectorTileUploader {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileUploader.class);
    private final StorageClient storageClient;

    public VectorTileUploader(final StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    public void upload(final String path, final Collection<BinaryTile> binaryTiles) {
        final AtomicInteger count = new AtomicInteger();
        final long startTime = System.currentTimeMillis();
        final int onePercent = max(1, binaryTiles.size() / 100);
        logger.info("Starting upload. {} tiles in total", binaryTiles.size());
        final ForkJoinPool customThreadPool = new ForkJoinPool(12);
        try {
            customThreadPool
                    .submit(
                            () ->
                                    binaryTiles.parallelStream()
                                            .forEach(
                                                    tile -> { // Parallel for-each loop
                                                        // Upload tile
                                                        try {
                                                            upload(path, tile);
                                                        } catch (final IOException e) {
                                                            throw new UncheckedIOException(e);
                                                        }

                                                        // Log progress
                                                        final int countValue =
                                                                count.incrementAndGet();
                                                        if (countValue % onePercent == 0) {
                                                            final int percent =
                                                                    100
                                                                            * countValue
                                                                            / binaryTiles.size();
                                                            final long currentTime =
                                                                    System.currentTimeMillis();
                                                            final double tilesPerSecond =
                                                                    countValue
                                                                            / ((double)
                                                                                    (currentTime
                                                                                            - startTime))
                                                                            * 1000.0;
                                                            logger.info(
                                                                    "Uploaded {}% ({} tiles per"
                                                                            + " second)",
                                                                    percent, tilesPerSecond);
                                                        }
                                                    }))
                    .get();
        } catch (final InterruptedException e) {
            logger.error("Exception uploading tiles: ", e);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            logger.error("Exception uploading tiles: ", e);
        }
        logger.info("Upload done");
    }

    public void upload(final String path, final BinaryTile tile) throws IOException {
        final String tilePath = Paths.get(path, tile.info().path()).toString();
        final String contentType = "application/protobuf";
        storageClient.write(new ByteOutput(tile.bytes(), contentType), tilePath);
    }
}
