package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TileJsonVectorLayerBuilderTest {

    @Test
    void testBuildMandatoryFieldsOnly() {
        final var builder = new TileJsonVectorLayerBuilder("layer1");
        final var vectorLayer = builder.build();

        assertEquals("layer1", vectorLayer.id());
        assertNull(vectorLayer.description());
        assertNull(vectorLayer.minZoom());
        assertNull(vectorLayer.maxZoom());
        assertTrue(vectorLayer.fields().isEmpty());
    }

    @Test
    void testBuildAllFields() {
        final var builder =
                new TileJsonVectorLayerBuilder("layer1")
                        .addLevel(1)
                        .addLevel(1)
                        .addLevel(5)
                        .addLevel(3)
                        .addLevel(2)
                        .addField("field1", "value1")
                        .addField("field2", 100)
                        .addField("field5", 101)
                        .addField("field5", 102.0)
                        .addField("field5", 103.0)
                        .addField("field5", 104.0)
                        .addField("field5", 105.0)
                        .addField("field5", 106.0)
                        .addField("field6", 101)
                        .addField("field6", 102.0)
                        .addField("field6", 103.0)
                        .addField("field6", "String")
                        .addField("field6", 105.0)
                        .addField("field6", 106.0)
                        .addField("field3", true)
                        .addField("field3", false)
                        .addField("field1", "value2");

        final var vectorLayer = builder.build();

        assertEquals("layer1", vectorLayer.id());
        assertEquals(1, vectorLayer.minZoom());
        assertEquals(5, vectorLayer.maxZoom());
        assertEquals("One of: value1, value2", vectorLayer.fields().get("field1"));
        assertEquals("100", vectorLayer.fields().get("field2"));
        assertEquals("Boolean", vectorLayer.fields().get("field3"));
        assertEquals("Number", vectorLayer.fields().get("field5"));
        assertEquals("Any", vectorLayer.fields().get("field6"));
    }

    @Test
    void testBuildMissingId() {
        final var builder = new TileJsonVectorLayerBuilder("");
        assertThrows(IllegalStateException.class, builder::build, "id must not be blank");
    }
}
