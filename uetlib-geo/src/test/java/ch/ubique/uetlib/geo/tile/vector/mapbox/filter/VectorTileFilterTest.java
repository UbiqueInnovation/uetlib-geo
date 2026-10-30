package ch.ubique.uetlib.geo.tile.vector.mapbox.filter;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.uetlib.core.jackson.JsonMapperUtils;
import ch.ubique.uetlib.geo.tile.vector.mapbox.decoding.VectorTileDecoder;
import ch.ubique.uetlib.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.FilterDefinition;
import ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRule;
import ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.TagFilterRule;
import ch.ubique.uetlib.geo.tile.vector.mapbox.models.VectorTile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

class VectorTileFilterTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperUtils.jsonMapper();

    private static final Path TEST_OUTPUT_DIR = Path.of("/tmp", "vector-tiles-filter-test");

    private static final Map<String, FilterDefinition> FILTER_DEFINITIONS =
            Map.ofEntries(
                    Map.entry("all", FilterDefinition.withRules().includeOthers()),
                    Map.entry("none", FilterDefinition.withRules().excludeOthers()),
                    Map.entry(
                            "all-no-tags",
                            FilterDefinition.withRules(
                                            FilterRule.include().tagRule(TagFilterRule.deleteAll()))
                                    .excludeOthers()),
                    Map.entry(
                            "complex",
                            FilterDefinition.withRules(
                                            FilterRule.include()
                                                    .layer("boundary")
                                                    .tag("admin_level", 2),
                                            FilterRule.exclude()
                                                    .layers(
                                                            "boundary",
                                                            "aerodrome_label",
                                                            "area_name",
                                                            "bathymetry",
                                                            "extent",
                                                            "housenumber",
                                                            "mountain_peak",
                                                            "park",
                                                            "poi",
                                                            "shortest_path",
                                                            "spot_elevation",
                                                            "transportation_name",
                                                            "contour_line",
                                                            "contour_line_pt"))
                                    .includeOthers()));

    @ParameterizedTest
    @ValueSource(strings = {"swisstopo/base/6/33/22.pbf", "swisstopo/base/13/4290/2868.pbf"})
    void testFilter(final String vectorTilePath) throws Exception {
        final var originalTile = decodeTile(vectorTilePath);
        assertNotNull(originalTile);

        for (final var entry : FILTER_DEFINITIONS.entrySet()) {
            final var name = entry.getKey();
            final var filterDefinition = entry.getValue();
            final var filter = new VectorTileFilter(filterDefinition);

            final var filteredTile = filter.filter(originalTile);

            encodeTile(Path.of(name, vectorTilePath), filteredTile);

            OBJECT_MAPPER.writeValue(
                    TEST_OUTPUT_DIR.resolve(name + "/filter.json").toFile(), filterDefinition);
        }
    }

    private VectorTile decodeTile(final String resourcePath) throws IOException {
        try (final var inputStream =
                getClass().getResourceAsStream("/vector-tiles/" + resourcePath)) {
            return VectorTileDecoder.decode(inputStream);
        }
    }

    private static void encodeTile(final Path vectorTilePath, final VectorTile tile)
            throws IOException {
        final var outputPath = TEST_OUTPUT_DIR.resolve(vectorTilePath);
        FileUtils.createParentDirectories(outputPath.toFile());
        try (final var outputStream = Files.newOutputStream(outputPath)) {
            VectorTileEncoder.encode(tile).writeTo(outputStream);
        }
    }
}
