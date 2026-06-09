package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingVertex;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Map;

class GeometryTileEncoderTest {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testBasicEncoding() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(0.0, 0.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(10.0, 10.0));
        final var line =
                GF.createLineString(
                        new Coordinate[] {new Coordinate(0, 0), new Coordinate(10, 10)});
        final Map<String, Object> tags = Map.of("name", "test");
        final var edge =
                new SmartDrawingEdge(v1, v2, line, tags, new int[] {0}, new int[] {0}, null, new boolean[0]);

        final var tile =
                new GeometryTileEncoder(envelope)
                        .extent(100)
                        .encodeElevation(false)
                        .add(edge)
                        .encode();

        assertThat(tile.getExtent()).isEqualTo(100);
        assertThat(tile.getWaysCount()).isEqualTo(1);
        final var way = tile.getWays(0);
        assertThat(way.getU()).isEqualTo(1);
        assertThat(way.getV()).isEqualTo(2);

        // Tags: "name" is key 0, "test" is value 0
        assertThat(way.getTagsList()).containsExactly(0, 0);
        assertThat(tile.getKeys(0)).isEqualTo("name");
        assertThat(tile.getValues(0).getStringValue()).isEqualTo("test");

        // Coordinates:
        // (0,0) -> (0, 100) in screen (flipped Y)
        // (10,10) -> (100, 0) in screen
        // Zig-zag:
        // (0, 100) -> [zigzag(0)=0, zigzag(100)=200]
        // (100-0, 0-100) = (100, -100) -> [zigzag(100)=200, zigzag(-100)=199]
        assertThat(way.getEncodedCoordinatesList()).containsExactly(0, 200, 200, 199);
    }

    @Test
    void test3DEncoding() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(0.0, 0.0, 1.0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(10.0, 10.0, 3.0));
        final var line =
                GF.createLineString(
                        new Coordinate[] {v1.coordinate().toJts(), v2.coordinate().toJts()});
        final Map<String, Object> tags = Map.of();
        final var edge =
                new SmartDrawingEdge(v1, v2, line, tags, new int[] {0}, new int[] {0}, null, new boolean[0]);

        final var tile = new GeometryTileEncoder(envelope).extent(100).add(edge).encode();

        final var way = tile.getWays(0);
        // Coordinates:
        // [0, 100, 100], [100, 0, 300]
        // zig-zag(0)=0, zig-zag(100)=200, zig-zag(100)=200
        // zig-zag(100-0)=200, zig-zag(0-100)=199, zig-zag(300-100=200)=400
        assertThat(way.getEncodedCoordinatesList()).containsExactly(0, 200, 200, 200, 199, 400);
    }

    @Test
    void testMultiEdgeVariant() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(0, 0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(1, 1));
        final var line =
                GF.createLineString(
                        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        final Map<String, Object> tags = Map.of();
        final var edge =
                new SmartDrawingEdge(v1, v2, line, tags, new int[] {0}, new int[] {0}, 123L, new boolean[0]);

        final var tile =
                new GeometryTileEncoder(envelope)
                        .encodeElevation(false)
                        .add(edge)
                        .encode();

        assertThat(tile.getWays(0).getMultiEdgeVariant()).isEqualTo(123L);
    }

    @Test
    void testDifferentValueTypes() throws Exception {
        final var crs = CRS.decode("EPSG:4326", true);
        final var envelope = new ReferencedEnvelope(0, 10, 0, 10, crs);
        final var v1 = new SmartDrawingVertex(1, new WGS84Coordinate(0, 0));
        final var v2 = new SmartDrawingVertex(2, new WGS84Coordinate(1, 1));
        final var line =
                GF.createLineString(
                        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        final Map<String, Object> tags =
                Map.of("str", "val", "int", 123, "float", 1.23f, "bool", true);
        final var edge =
                new SmartDrawingEdge(v1, v2, line, tags, new int[] {0}, new int[] {0}, null, new boolean[0]);

        final var tile =
                new GeometryTileEncoder(envelope)
                        .extent(2048)
                        .encodeElevation(false)
                        .add(edge)
                        .encode();
        assertThat(tile.getExtent()).isEqualTo(2048);
        assertThat(tile.getKeysList()).containsExactlyInAnyOrder("str", "int", "float", "bool");

        for (var i = 0; i < tile.getValuesCount(); i++) {
            final var key = tile.getKeys(i);
            final var val = tile.getValues(i);
            switch (key) {
                case "str" -> assertThat(val.getStringValue()).isEqualTo("val");
                case "int" -> assertThat(val.getIntValue()).isEqualTo(123L);
                case "float" -> assertThat(val.getFloatValue()).isEqualTo(1.23f);
                case "bool" -> assertThat(val.getBoolValue()).isTrue();
                default -> fail();
            }
        }
    }
}
