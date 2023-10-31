package ch.ubique.backend.geo.coordinate.models;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

class CoordinateObjectMapperTest {

    @Test
    void testLV03() throws JsonProcessingException {

        final var c1 = new LV03Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new LV03Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testLV95() throws JsonProcessingException {

        final var c1 = new LV95Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new LV95Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testUTM32() throws JsonProcessingException {

        final var c1 = new UTM32Coordinate(1, 2);
        final var c1Json = "{\"east\":1.0,\"north\":2.0}";

        final var c2 = new UTM32Coordinate(1, 2, 3.0);
        final var c2Json = "{\"east\":1.0,\"north\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testWebMercator() throws JsonProcessingException {

        final var c1 = new WebMercatorCoordinate(1, 2);
        final var c1Json = "{\"x\":1.0,\"y\":2.0}";

        final var c2 = new WebMercatorCoordinate(1, 2, 3.0);
        final var c2Json = "{\"x\":1.0,\"y\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    @Test
    void testWGS84() throws JsonProcessingException {

        final var c1 = new WGS84Coordinate(1, 2);
        final var c1Json = "{\"latitude\":1.0,\"longitude\":2.0}";

        final var c2 = new WGS84Coordinate(1, 2, 3.0);
        final var c2Json = "{\"latitude\":1.0,\"longitude\":2.0,\"elevation\":3.0}";

        testSerializeAndDeserialize(c1Json, c1);
        testSerializeAndDeserialize(c2Json, c2);
    }

    private static <C extends Coordinate> void testSerializeAndDeserialize(
            final String json, final C coordinate) throws JsonProcessingException {
        final var objectMapper = ObjectMapperUtils.objectMapper();

        final var serialized = objectMapper.writeValueAsString(coordinate);
        final var deserialized = objectMapper.readValue(json, coordinate.getClass());

        assertEquals(json, serialized);
        assertEquals(coordinate, deserialized);

        assertEquals(coordinate, objectMapper.readValue(serialized, coordinate.getClass()));
        assertEquals(json, objectMapper.writeValueAsString(deserialized));
    }
}
