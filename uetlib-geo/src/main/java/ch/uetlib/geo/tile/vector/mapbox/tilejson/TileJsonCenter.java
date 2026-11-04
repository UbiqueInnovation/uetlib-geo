package ch.uetlib.geo.tile.vector.mapbox.tilejson;

import ch.uetlib.geo.coordinate.models.WGS84Coordinate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public record TileJsonCenter(WGS84Coordinate coordinate, int zoom) {

    @JsonCreator
    public TileJsonCenter(final List<Double> center) {
        this(new WGS84Coordinate(center.get(1), center.get(0)), center.get(2).intValue());
    }

    @JsonValue
    public Object[] jsonValue() {
        return new Object[] {coordinate.longitude(), coordinate.latitude(), zoom};
    }
}
