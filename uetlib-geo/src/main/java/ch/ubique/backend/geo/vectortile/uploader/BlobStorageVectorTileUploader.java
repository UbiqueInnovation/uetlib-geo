package ch.ubique.backend.geo.vectortile.uploader;

import static java.lang.Math.max;

import ch.ubique.backend.geo.vectortile.models.BinaryTile;
import ch.ubique.backend.output.blob.service.BlobStorageService;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.BlockBlobItem;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class BlobStorageVectorTileUploader implements VectorTileUploader {

    private static final Logger logger =
            LoggerFactory.getLogger(BlobStorageVectorTileUploader.class);
    private final String container;
    private final BlobStorageService blobStorageService;

    public BlobStorageVectorTileUploader(BlobStorageService blobStorageService, String container) {
        this.blobStorageService = blobStorageService;
        this.container = container;
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
                                                        upload(path, tile);

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
                                                                    "Uploaded {}% ({} tiles per second)",
                                                                    percent, tilesPerSecond);
                                                        }
                                                    }))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Exception uploading tiles: ", e);
        }
        logger.info("Upload done");
    }

    private Mono<Response<BlockBlobItem>> uploadAsync(String path, BinaryTile tile) {
        String tilePath = Paths.get(path, tile.getPath()).toString();
        String contentType = "application/protobuf";
        return blobStorageService.uploadAsync(tile.getBytes(), container, tilePath, contentType);
    }

    @Override
    public void prepareUpload(String path) {
        /* Nothing to do here */
    }

    public void upload(String path, BinaryTile tile) {
        String tilePath = Paths.get(path, tile.getPath()).toString();
        String contentType = "application/protobuf";
        blobStorageService.upload(tile.getBytes(), container, tilePath, contentType);
    }
}
