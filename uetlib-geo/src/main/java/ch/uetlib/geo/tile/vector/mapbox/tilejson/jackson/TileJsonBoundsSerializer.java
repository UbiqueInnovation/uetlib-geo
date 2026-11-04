package ch.uetlib.geo.tile.vector.mapbox.tilejson.jackson;

import ch.uetlib.geo.coordinate.models.BoundingBox;
import ch.uetlib.geo.coordinate.models.WGS84Coordinate;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.List;

public class TileJsonBoundsSerializer extends ValueSerializer<BoundingBox<WGS84Coordinate>> {

    @Override
    public void serialize(
            final BoundingBox<WGS84Coordinate> value,
            final JsonGenerator gen,
            final SerializationContext serializers) {
        // left, bottom, right, top.
        final var list =
                List.of(
                        value.min().longitude(),
                        value.min().latitude(),
                        value.max().longitude(),
                        value.max().latitude());

        serializers.findValueSerializer(list.getClass()).serialize(list, gen, serializers);
    }
}
