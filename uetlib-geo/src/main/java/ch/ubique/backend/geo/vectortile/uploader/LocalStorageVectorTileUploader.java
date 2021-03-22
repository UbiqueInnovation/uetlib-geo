package ch.ubique.backend.geo.vectortile.uploader;

import static java.lang.Math.max;

import ch.ubique.backend.geo.vectortile.models.BinaryTile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalStorageVectorTileUploader implements VectorTileUploader {

	private final Path outputBaseDir;

	private static final Logger logger = LoggerFactory.getLogger(LocalStorageVectorTileUploader.class);

	public LocalStorageVectorTileUploader(Path outputBaseDir) {
		if (!Files.exists(outputBaseDir)) {
			logger.info("Base dir does not exist. Try to create");
			try {
				Files.createDirectories(outputBaseDir);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		this.outputBaseDir = outputBaseDir;
	}

	@Override
	public void prepareUpload(String path) {
		Path destinationPath = outputBaseDir.resolve(path);
		try {
			Files.createDirectories(destinationPath);
		} catch (IOException ioe) {
			logger.error("Could not write file: {}", path, ioe);
		}
	}

	@Override
	public void upload(String path, BinaryTile binaryTile) {
		Path destinationPath = outputBaseDir.resolve(path);
		Path tileDestination = destinationPath.resolve(binaryTile.getPath());
		try {
			Files.createDirectories(tileDestination.getParent());
			Files.write(tileDestination, binaryTile.getBytes());
		} catch (IOException e) {
			logger.error("Could not write file: {}", tileDestination, e);
		}
	}

	@Override
	public void upload(String path, Collection<BinaryTile> binaryTiles) {
		Path destinationPath = outputBaseDir.resolve(path);
		try {
			Files.createDirectories(destinationPath);
			final AtomicInteger count = new AtomicInteger();
			int onePercent = max(1, binaryTiles.size() / 100);
			logger.info("Starting upload. {} tiles in total", binaryTiles.size());
			binaryTiles.parallelStream().forEach(tile -> { // Parallel for-each loop

				// write tile
				Path tileDestination = destinationPath.resolve(tile.getPath());
				try {
					Files.createDirectories(tileDestination.getParent());
					Files.write(tileDestination, tile.getBytes());
				} catch (IOException e) {
					logger.error("Could not write file: {}", tileDestination, e);
				}

				// Log progress
				int countValue = count.incrementAndGet();
				if (countValue % onePercent == 0) {
					int percent = 100 * countValue / binaryTiles.size();
					logger.info("Uploaded {}%", percent);
				}
			});
			logger.info("Upload done");
		} catch (IOException ioe) {
			logger.error("Could not write file: {}", path, ioe);
		}
	}

}
