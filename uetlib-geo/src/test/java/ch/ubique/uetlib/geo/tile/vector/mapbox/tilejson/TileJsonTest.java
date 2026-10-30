package ch.ubique.uetlib.geo.tile.vector.mapbox.tilejson;

import static org.assertj.core.api.IterableAssert.assertThatIterable;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.uetlib.core.jackson.JsonMapperUtils;
import ch.ubique.uetlib.core.utils.ValidationUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import tools.jackson.databind.ObjectMapper;

class TileJsonTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperUtils.jsonMapper();

    @ParameterizedTest
    @ValueSource(strings = {"osm", "ch.swisstopo.base.vt", "ch.swisstopo.relief.vt"})
    void testTileJson(final String id) throws IOException, JSONException {
        final var jsonString = readToString(id);
        final var tileJson = OBJECT_MAPPER.readValue(jsonString, TileJson.class);
        assertNotNull(tileJson);
        assertThatIterable(ValidationUtils.validate(tileJson)).isEmpty();

        final var serialized = OBJECT_MAPPER.writeValueAsString(tileJson);

        JSONAssert.assertEquals(jsonString, serialized, false);
    }

    public String readToString(final String id) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream("/tilejson/" + id + ".json")) {
            assertNotNull(inputStream);
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
