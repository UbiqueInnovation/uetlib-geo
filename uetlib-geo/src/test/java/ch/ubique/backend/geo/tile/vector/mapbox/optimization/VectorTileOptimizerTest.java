package ch.ubique.backend.geo.tile.vector.mapbox.optimization;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.tile.vector.mapbox.decoding.VectorTileDecoder;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTile;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class VectorTileOptimizerTest {

    private static final Path TEST_OUTPUT_DIR = Path.of("/tmp", "vector-tiles-filter-test");

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ubique/post/poi/7/66/44.pbf",
                "ubique/post/poi/7/66/45.pbf",
                "ubique/post/poi/7/67/44.pbf",
                "ubique/post/poi/7/67/45.pbf",
                "swisstopo/base/6/33/22.pbf",
                "swisstopo/base/10/536/358.pbf",
                "swisstopo/base/13/4290/2868.pbf",
                "swisstopo/relief/13/4290/2868.pbf",
            })
    void decode(final String resourcePath) throws Exception {
        final var decoded = decodeTile(resourcePath);
        assertNotNull(decoded);

        final var optimized = VectorTileOptimizer.optimize(decoded);
        assertNotNull(optimized);

        encodeTile(Path.of(resourcePath), optimized);
    }

    private VectorTile decodeTile(final String resourcePath) throws IOException {
        try (final var inputStream =
                getClass().getResourceAsStream("/vector-tiles/" + resourcePath)) {
            return VectorTileDecoder.decode(inputStream);
        }
    }

    private static void encodeTile(final Path vectorTilePath, final VectorTile tile)
            throws IOException {
        final var outputPath = TEST_OUTPUT_DIR.resolve("optimized").resolve(vectorTilePath);
        FileUtils.createParentDirectories(outputPath.toFile());
        try (final var outputStream = Files.newOutputStream(outputPath)) {
            VectorTileEncoder.encode(tile).writeTo(outputStream);
        }
    }
}
