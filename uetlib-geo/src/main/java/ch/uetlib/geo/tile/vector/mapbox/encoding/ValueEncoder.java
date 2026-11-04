package ch.uetlib.geo.tile.vector.mapbox.encoding;

import static ch.uetlib.geo.tile.vector.mapbox.VectorTileEncodingConfig.allowUnsignedNumbers;

import ch.uetlib.geo.vectortile.protobuf.Tile;

final class ValueEncoder {

    private ValueEncoder() {}

    public static Tile.Value encodeValue(final Object value) {
        final var tileValue = Tile.Value.newBuilder();
        switch (value) {
            case final String string -> tileValue.setStringValue(string);
            case final Integer intValue when allowUnsignedNumbers && intValue >= 0 ->
                    tileValue.setUintValue(intValue);
            case final Integer intValue -> tileValue.setSintValue(intValue);
            case final Long longValue when allowUnsignedNumbers && longValue >= 0 ->
                    tileValue.setUintValue(longValue);
            case final Long longValue -> tileValue.setSintValue(longValue);
            case final Float floatValue -> tileValue.setFloatValue(floatValue);
            case final Double doubleValue -> tileValue.setDoubleValue(doubleValue);
            case final Number number -> tileValue.setStringValue(number.toString());
            case final Boolean booleanValue -> tileValue.setBoolValue(booleanValue);
            default -> tileValue.setStringValue(value.toString());
        }
        return tileValue.build();
    }
}
