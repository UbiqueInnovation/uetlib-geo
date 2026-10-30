package ch.ubique.uetlib.geo.gdal;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.uetlib.geo.gdal.warp.GdalResamplingMethod;
import ch.ubique.uetlib.geo.gdal.warp.GdalWarpOptions;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

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
