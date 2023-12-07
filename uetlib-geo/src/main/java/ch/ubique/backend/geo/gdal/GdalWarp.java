package ch.ubique.backend.geo.gdal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GdalWarp {
    public static final Path gdalwarpTempDir =
            Path.of(System.getProperty("java.io.tmpdir"), "/gdalwarp");
    private static final Logger logger = LoggerFactory.getLogger(GdalWarp.class);

    private GdalWarp() {}

    public static Path convert(final GdalWarpOptions options, final Duration timeout)
            throws IOException, InterruptedException {
        final var builder = new ProcessBuilder();
        final var command = options.buildCommand();

        builder.command(command);
        builder.directory(gdalwarpTempDir.toFile());
        final var process = builder.start();

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(
                    () -> {
                        try (final var inputReader = process.inputReader()) {
                            inputReader
                                    .lines()
                                    .forEach(line -> logger.info("gdalwarp stdout: {}", line));
                        } catch (final IOException e) {
                            logger.warn("Error reading gdalwarp stdout", e);
                        }
                        try (final var errorReader = process.errorReader()) {
                            errorReader
                                    .lines()
                                    .forEach(line -> logger.warn("gdalwarp stderr: {}", line));
                        } catch (final IOException e) {
                            logger.warn("Error reading gdalwarp stderr", e);
                        }
                    });
        }
        final var successfulTermination = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
        if (!successfulTermination) {
            throw new IllegalStateException("gdalwarp returned error code: " + process.exitValue());
        }
        return options.targetFile();
    }
}
