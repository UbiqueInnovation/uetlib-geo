package ch.uetlib.geo.mbtile.metadata.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

class MetadataJsonTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void roundTripsNestedMetadataWithExistingPropertyNames() {
        final String json =
                """
                {
                  "vector_layers": [{
                    "id": "roads", "description": "Road network", "minzoom": 0, "maxzoom": 14,
                    "fields": {"name": "String", "lanes": "Number", "paved": "Boolean"}
                  }],
                  "tilestats": {
                    "layerCount": 1,
                    "layers": [{
                      "layer": "roads", "count": 2, "geometry": "LineString", "attributeCount": 1,
                      "attributes": [{
                        "attribute": "lanes", "count": 2, "type": "number",
                        "values": [1, 2], "min": 1, "max": 2
                      }]
                    }]
                  }
                }
                """;
        final var metadata = mapper.readValue(json, MetadataJson.class);
        assertEquals(mapper.readTree(json), mapper.readTree(mapper.writeValueAsString(metadata)));
        assertEquals(
                metadata,
                mapper.readValue(mapper.writeValueAsString(metadata), MetadataJson.class));
    }

    @Test
    void preservesMissingOptionalPropertiesAndIgnoresUnknownProperties() {
        final var metadata =
                mapper.readValue(
                        """
                        {"vector_layers": [{"id": "roads", "fields": {"name": "String"}, "future": true}],
                         "future": true}
                        """,
                        MetadataJson.class);
        assertNull(metadata.tilestats());
        assertEquals(
                mapper.readTree(
                        """
                        {"vector_layers": [{"id": "roads", "fields": {"name": "String"}}]}
                        """),
                mapper.readTree(mapper.writeValueAsString(metadata)));
        for (final var type : new Class<?>[] {Attribute.class, Layer.class, Tilestats.class}) {
            assertEquals(
                    mapper.readTree("{}"),
                    mapper.readTree(mapper.writeValueAsString(mapper.readValue("{}", type))));
        }
    }

    @Test
    void rejectsMissingRequiredProperties() {
        assertThrows(
                MismatchedInputException.class, () -> mapper.readValue("{}", MetadataJson.class));
        assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue("{\"fields\": {\"name\": \"String\"}}", VectorLayer.class));
        assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue("{\"id\": \"roads\"}", VectorLayer.class));
    }
}
