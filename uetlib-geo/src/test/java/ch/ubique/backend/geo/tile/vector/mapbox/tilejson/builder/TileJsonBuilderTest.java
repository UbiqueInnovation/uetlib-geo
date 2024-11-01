package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.builder;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJsonCenter;

import org.junit.jupiter.api.Test;

import java.util.List;

class TileJsonBuilderTest {

    @Test
    void testBuildMandatoryFieldsOnly() {
        final var builder = new TileJsonBuilder().tiles("https://example.com/tiles");
        final var tileJson = builder.build();

        assertEquals("3.0.0", tileJson.tileJsonVersion());
        assertEquals(List.of("https://example.com/tiles"), tileJson.tiles());
        assertTrue(tileJson.vectorLayers().isEmpty());
    }

    @Test
    void testBuildAllFields() {
        final var builder =
                new TileJsonBuilder()
                        .tiles("https://example.com/tiles")
                        .attribution("Sample Attribution")
                        .bounds(
                                new BoundingBox<>(
                                        new WGS84Coordinate(0, 0), new WGS84Coordinate(10, 10)))
                        .center(new TileJsonCenter(new WGS84Coordinate(5, 5), 5))
                        .data(List.of("data1", "data2"))
                        .description("Sample Description")
                        .fillZoom(10)
                        .grids(List.of("grid1", "grid2"))
                        .legend("Sample Legend")
                        .maxZoom(15)
                        .minZoom(5)
                        .name("Sample Name")
                        .scheme("xyz")
                        .template("Template")
                        .version("1.0")
                        .additionalData("extraKey", "extraValue");

        final var tileJson = builder.build();

        assertEquals("Sample Attribution", tileJson.attribution());
        assertNotNull(tileJson.bounds());
        assertNotNull(tileJson.center());
        assertEquals("Sample Description", tileJson.description());
        assertEquals(10, tileJson.fillzoom());
        assertEquals(List.of("grid1", "grid2"), tileJson.grids());
        assertEquals("Sample Legend", tileJson.legend());
        assertEquals(15, tileJson.maxZoom());
        assertEquals(5, tileJson.minZoom());
        assertEquals("Sample Name", tileJson.name());
        assertEquals("xyz", tileJson.scheme());
        assertEquals("Template", tileJson.template());
        assertEquals("1.0", tileJson.version());
        assertEquals("extraValue", tileJson.additionalData().get("extraKey"));
    }

    @Test
    void testMissingMandatoryFields() {
        final var builder = new TileJsonBuilder();
        assertThrows(
                IllegalStateException.class, builder::build, "tiles must not be null or empty");
    }
}
