package ch.ubique.backend.geo.gdal;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.gdal.warp.GdalResamplingMethod;
import ch.ubique.backend.geo.gdal.warp.GdalWarpOptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Path;

class GdalTest {
    @Test
    @EnabledIf(
            value = "isGdalWarpAvailable",
            disabledReason = "Disabled because it requires GDAL to be installed")
    void testWarp() {
        final var options =
                GdalWarpOptions.defaults()
                        .sourceFile(Path.of("src/test/resources/gdal/gdal_ww_test.grib2"))
                        .targetFile(Path.of("src/test/resources/gdal/gdal_ww_test_output.tif"))
                        .targetCoordinateSystem(CoordinateSystems.WEB_MERCATOR)
                        .resamplingMethod(GdalResamplingMethod.NEAREST);
        assertDoesNotThrow(() -> Gdal.warp(options));
    }

    public boolean isGdalWarpAvailable() {
        return Gdal.isWarpAvailable();
    }
}
