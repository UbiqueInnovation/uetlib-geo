package ch.ubique.backend.geo.vectortile.uploader;

import ch.ubique.backend.geo.vectortile.models.BinaryTile;
import ch.ubique.backend.output.StorageClient;
import ch.ubique.backend.output.output.ByteOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.max;

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
                                                            throw new RuntimeException(e);
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
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Exception uploading tiles: ", e);
        }
        logger.info("Upload done");
    }

    public void upload(String path, BinaryTile tile) throws IOException {
        String tilePath = Paths.get(path, tile.getPath()).toString();
        String contentType = "application/protobuf";
        storageClient.write(new ByteOutput(tile.getBytes(), contentType), tilePath);
    }
}
