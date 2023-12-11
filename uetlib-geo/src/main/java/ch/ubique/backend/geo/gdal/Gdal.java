package ch.ubique.backend.geo.gdal;

import ch.ubique.backend.geo.gdal.warp.GdalWarpOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Gdal {
    private static final Logger logger = LoggerFactory.getLogger(Gdal.class);

    private Gdal() {}

    public static void warp(final GdalWarpOptions options)
            throws IOException, InterruptedException {

        executeProcess(options.buildCommand(), options.timeout());
    }

    public static boolean isWarpAvailable() {
        return isLibAvailable("gdalwarp");
    }

    public static boolean isLibAvailable(final String binPath) {
        try {
            executeProcess(List.of(binPath, " -version"), Duration.ofSeconds(1));
            return true;
        } catch (final Exception e) {
            logger.error("Failed to execute {}", binPath, e);
            return false;
        }
    }

    private static void executeProcess(final List<String> command, final Duration timeout)
            throws IOException, InterruptedException {

        logger.info("Running command: {}", command);
        final var builder = new ProcessBuilder(command);
        final var process = builder.start();

        Executors.newSingleThreadExecutor().submit(() -> logProcessOutputsAndErrors(process));

        final var successful = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);

        if (!successful) {
            throw new IllegalStateException(
                    process + " exited with error code: " + process.exitValue());
        }
    }

    private static void logProcessOutputsAndErrors(final Process process) {
        try (final var inputReader = process.inputReader()) {
            inputReader.lines().forEach(line -> logger.info("stdout: {}", line));
        } catch (final IOException e) {
            logger.warn("Error reading stdout", e);
        }
        try (final var errorReader = process.errorReader()) {
            errorReader.lines().forEach(line -> logger.warn("stderr: {}", line));
        } catch (final IOException e) {
            logger.warn("Error reading stderr", e);
        }
    }
}
