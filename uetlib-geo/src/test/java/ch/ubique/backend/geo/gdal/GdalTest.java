package ch.ubique.backend.geo.gdal;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.gdal.warp.GdalResamplingMethod;
import ch.ubique.backend.geo.gdal.warp.GdalWarpOptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Path;

public class GdalTest {
    @Test
    @EnabledIf(
            value = "isGdalWarpAvailable",
            disabledReason = "Disabled because it requires GDAL to be installed")
    void testWarp() throws Exception {
        final var options =
                GdalWarpOptions.builder()
                        .sourceFile(Path.of("src/test/resources/gdal/gdal_ww_test.grib2"))
                        .targetFile(Path.of("src/test/resources/gdal/gdal_ww_test_output.tif"))
                        .targetSpatialReference(CoordinateSystems.WEB_MERCATOR)
                        .resamplingMethod(GdalResamplingMethod.NEAREST)
                        .build();
        Gdal.warp(options);
    }

    public boolean isGdalWarpAvailable() {
        return Gdal.isWarpAvailable();
    }
}
