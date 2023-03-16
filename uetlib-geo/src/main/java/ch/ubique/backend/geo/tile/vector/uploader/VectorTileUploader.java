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

    public VectorTileUploader(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    public void upload(String path, Collection<BinaryTile> binaryTiles) {
        final AtomicInteger count = new AtomicInteger();
        final long startTime = System.currentTimeMillis();
        int onePercent = max(1, binaryTiles.size() / 100);
        logger.info("Starting upload. {} tiles in total", binaryTiles.size());
        ForkJoinPool customThreadPool = new ForkJoinPool(12);
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
                                                        } catch (IOException e) {
                                                            throw new UncheckedIOException(e);
                                                        }

                                                        // Log progress
                                                        int countValue = count.incrementAndGet();
                                                        if (countValue % onePercent == 0) {
                                                            int percent =
                                                                    100
                                                                            * countValue
                                                                            / binaryTiles.size();
                                                            long currentTime =
                                                                    System.currentTimeMillis();
                                                            double tilesPerSecond =
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
        } catch (InterruptedException e) {
            logger.error("Exception uploading tiles: ", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("Exception uploading tiles: ", e);
        }
        logger.info("Upload done");
    }

    public void upload(String path, BinaryTile tile) throws IOException {
        String tilePath = Paths.get(path, tile.info().path()).toString();
        String contentType = "application/protobuf";
        storageClient.write(new ByteOutput(tile.bytes(), contentType), tilePath);
    }
}
