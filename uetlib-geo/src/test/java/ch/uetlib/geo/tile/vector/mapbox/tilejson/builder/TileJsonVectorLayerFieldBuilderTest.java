package ch.uetlib.geo.tile.vector.mapbox.tilejson.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.time.Instant;

class TileJsonVectorLayerFieldBuilderTest {

    @Test
    void test() {
        assertEquals(
                "One of: value1, value2",
                new TileJsonVectorLayerFieldBuilder().add("value1").add("value2").build());
        assertEquals("100", new TileJsonVectorLayerFieldBuilder().add(100).build());
        assertEquals("true", new TileJsonVectorLayerFieldBuilder().add(true).build());
        assertEquals("Boolean", new TileJsonVectorLayerFieldBuilder().add(true).add(false).build());
        assertEquals(
                "String",
                new TileJsonVectorLayerFieldBuilder()
                        .add("ab")
                        .add("cd")
                        .add("ef")
                        .add("gh")
                        .build());
        assertEquals(
                "Number",
                new TileJsonVectorLayerFieldBuilder().add(100).add(200).add(300).add(500).build());
        assertEquals(
                "Any",
                new TileJsonVectorLayerFieldBuilder()
                        .add("string")
                        .add(200)
                        .add(3.14)
                        .add(Instant.now())
                        .build());
    }

    @Test
    void testEmpty() {
        final var builder = new TileJsonVectorLayerFieldBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
