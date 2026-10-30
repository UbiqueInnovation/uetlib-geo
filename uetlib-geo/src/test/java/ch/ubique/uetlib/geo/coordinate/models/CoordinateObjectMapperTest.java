package ch.ubique.uetlib.geo.coordinate.models;

import static ch.ubique.uetlib.geo.jackson.JsonMapperJtsUtils.jsonMapperLv95;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class CoordinateObjectMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = jsonMapperLv95();

    @Test
    void testLV03() {

        final var c1 = new LV03Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new LV03Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testLV95() {

        final var c1 = new LV95Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new LV95Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testUTM32() {

        final var c1 = new UTM32Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new UTM32Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testWebMercator() {

        final var c1 = new WebMercatorCoordinate(1, 2);
        final var c1Json = "{\"x\":1.0,\"y\":2.0}";

        final var c2 = new WebMercatorCoordinate(1, 2, 3.0);
        final var c2Json = "{\"x\":1.0,\"y\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testWGS84() {

        final var c1 = new WGS84Coordinate(1, 2);
        final var c1Json = "{\"latitude\":1.0,\"longitude\":2.0}";

        final var c2 = new WGS84Coordinate(1, 2, 3.0);
        final var c2Json = "{\"latitude\":1.0,\"longitude\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    private static <C extends Coordinate> void testSerializeAndDeserialize(
            final String json, final C coordinate) {
        final var serialized = OBJECT_MAPPER.writeValueAsString(coordinate);
        final var deserialized = OBJECT_MAPPER.readValue(json, coordinate.getClass());

        assertEquals(json, serialized);
        assertEquals(coordinate, deserialized);

        assertEquals(coordinate, OBJECT_MAPPER.readValue(serialized, coordinate.getClass()));
        assertEquals(json, OBJECT_MAPPER.writeValueAsString(deserialized));
    }
}
