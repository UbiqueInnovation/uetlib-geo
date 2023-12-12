package ch.ubique.backend.geo.gdal;

import ch.ubique.backend.core.utils.ProcessUtils;
import ch.ubique.backend.geo.gdal.warp.GdalWarpOptions;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class Gdal {

    private Gdal() {}

    public static void warp(final GdalWarpOptions options) throws IOException {
        ProcessUtils.executeProcessAndLog(options.toCommand(), Duration.ofSeconds(10));
    }

    public static void warp(final GdalWarpOptions options, final Duration timeout)
            throws IOException {
        ProcessUtils.executeProcessAndLog(options.toCommand(), timeout);
    }

    public static boolean isWarpAvailable() {
        return isLibAvailable("gdalwarp");
    }

    public static boolean isLibAvailable(final String binPath) {
        final var command = List.of(binPath, "--version");
        try {
            final var result = ProcessUtils.executeProcessAndLog(command, Duration.ofSeconds(1));
            return result.getLeft() && result.getRight() == 0;
        } catch (final IOException e) {
            return false;
        }
    }
}
