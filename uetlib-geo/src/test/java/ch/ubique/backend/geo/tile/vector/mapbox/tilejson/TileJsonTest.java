package ch.ubique.backend.geo.tile.vector.mapbox.tilejson;

import static org.assertj.core.api.IterableAssert.assertThatIterable;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.core.utils.ValidationUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class TileJsonTest {

    @ParameterizedTest
    @ValueSource(strings = {"osm", "ch.swisstopo.base.vt", "ch.swisstopo.relief.vt"})
    void testTileJson(final String id) throws IOException, JSONException {
        final var objectMapper = ObjectMapperUtils.jsonMapper();

        final var jsonString = readToString(id);
        final var tileJson = objectMapper.readValue(jsonString, TileJson.class);
        assertNotNull(tileJson);
        assertThatIterable(ValidationUtils.validate(tileJson)).isEmpty();

        final var serialized = objectMapper.writeValueAsString(tileJson);

        JSONAssert.assertEquals(jsonString, serialized, false);
    }

    public String readToString(final String id) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream("/tilejson/" + id + ".json")) {
            assertNotNull(inputStream);
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
