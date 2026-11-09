package ch.uetlib.geo.tile.vector.mapbox.encoding;

import static org.junit.jupiter.api.Assertions.*;

import ch.uetlib.geo.tile.vector.mapbox.VectorTileEncodingConfig;
import ch.uetlib.geo.vectortile.protobuf.Tile;

import org.junit.jupiter.api.Test;

class ValueEncoderTest {

    @Test
    void encodeValueAllowUnsigned() {
        VectorTileEncodingConfig.allowUnsignedNumbers = true;
        assertEquals(
                Tile.Value.newBuilder().setSintValue(-1).build(), ValueEncoder.encodeValue(-1));
        assertEquals(Tile.Value.newBuilder().setUintValue(0).build(), ValueEncoder.encodeValue(0));
        assertEquals(Tile.Value.newBuilder().setUintValue(1).build(), ValueEncoder.encodeValue(1));

        assertEquals(
                Tile.Value.newBuilder().setSintValue(-1).build(), ValueEncoder.encodeValue(-1L));
        assertEquals(Tile.Value.newBuilder().setUintValue(0).build(), ValueEncoder.encodeValue(0L));
        assertEquals(Tile.Value.newBuilder().setUintValue(1).build(), ValueEncoder.encodeValue(1L));
        assertEquals(
                Tile.Value.newBuilder().setUintValue(Integer.MAX_VALUE + 1L).build(),
                ValueEncoder.encodeValue(Integer.MAX_VALUE + 1L));
    }

    @Test
    void encodeValueDisallowUnsigned() {
        VectorTileEncodingConfig.allowUnsignedNumbers = false;
        assertEquals(
                Tile.Value.newBuilder().setSintValue(-1).build(), ValueEncoder.encodeValue(-1));
        assertEquals(Tile.Value.newBuilder().setSintValue(0).build(), ValueEncoder.encodeValue(0));
        assertEquals(Tile.Value.newBuilder().setSintValue(1).build(), ValueEncoder.encodeValue(1));

        assertEquals(
                Tile.Value.newBuilder().setSintValue(-1).build(), ValueEncoder.encodeValue(-1L));
        assertEquals(Tile.Value.newBuilder().setSintValue(0).build(), ValueEncoder.encodeValue(0L));
        assertEquals(Tile.Value.newBuilder().setSintValue(1).build(), ValueEncoder.encodeValue(1L));

        assertEquals(
                Tile.Value.newBuilder().setSintValue(Integer.MAX_VALUE + 1L).build(),
                ValueEncoder.encodeValue(Integer.MAX_VALUE + 1L));
    }
}
