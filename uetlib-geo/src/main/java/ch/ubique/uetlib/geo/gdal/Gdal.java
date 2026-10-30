package ch.ubique.uetlib.geo.gdal;

import ch.ubique.uetlib.core.process.ProcessConfig;
import ch.ubique.uetlib.core.process.ProcessResult;
import ch.ubique.uetlib.core.utils.ProcessUtils;
import ch.ubique.uetlib.geo.gdal.warp.GdalWarpOptions;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class Gdal {

    private Gdal() {}

    public static ProcessResult warp(final GdalWarpOptions gdalWarpOptions) throws IOException {
        return warp(gdalWarpOptions, Duration.ofSeconds(10));
    }

    public static ProcessResult warp(final GdalWarpOptions gdalWarpOptions, final Duration timeout)
            throws IOException {
        final var config =
                ProcessConfig.builder()
                        .command(gdalWarpOptions.toCommand())
                        .timeout(timeout)
                        .build();
        return ProcessUtils.executeProcess(config);
    }

    public static ProcessResult warp(
            final GdalWarpOptions gdalWarpOptions,
            final ProcessConfig.ProcessConfigBuilder processConfigBuilder)
            throws IOException {
        final var config = processConfigBuilder.command(gdalWarpOptions.toCommand()).build();
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
