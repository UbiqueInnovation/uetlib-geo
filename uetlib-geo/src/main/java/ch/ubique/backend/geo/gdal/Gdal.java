package ch.ubique.backend.geo.gdal;

import ch.ubique.backend.core.process.ProcessConfig;
import ch.ubique.backend.core.process.ProcessResult;
import ch.ubique.backend.core.utils.ProcessUtils;
import ch.ubique.backend.geo.gdal.warp.GdalWarpOptions;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class Gdal {

    private Gdal() {
    }

    public static ProcessResult warp(final GdalWarpOptions options) throws IOException {
        return warp(options, Duration.ofSeconds(10));
    }

    public static ProcessResult warp(final GdalWarpOptions options, final Duration timeout)
            throws IOException {
        final var config =
                ProcessConfig.builder().command(options.toCommand()).timeout(timeout).build();
        return ProcessUtils.executeProcess(config);
    }

    public static boolean isWarpAvailable() {
        return isLibAvailable("gdalwarp");
    }

    public static boolean isLibAvailable(final String binPath) {
        final var command = List.of(binPath, "--version");
        final var config =
                ProcessConfig.builder().command(command).timeout(Duration.ofSeconds(1)).build();
        try {
            final var result = ProcessUtils.executeProcess(config);
            return result.success();
        } catch (final IOException e) {
            return false;
        }
    }
}
