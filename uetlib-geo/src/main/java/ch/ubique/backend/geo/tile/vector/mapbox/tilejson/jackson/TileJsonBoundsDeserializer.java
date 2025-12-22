package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.jackson;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.io.IOException;
import java.util.List;

public class TileJsonBoundsDeserializer extends ValueDeserializer<BoundingBox<WGS84Coordinate>> {

    @Override
    public BoundingBox<WGS84Coordinate> deserialize(
            final JsonParser p, final DeserializationContext deserializationContext)
            throws IOException {
        final var list = p.<List<Double>>readValueAs(new TypeReference<List<Double>>() {});
        final var left = list.getFirst();
        final var bottom = list.get(1);
        final var right = list.get(2);
        final var top = list.get(3);
        return new BoundingBox<>(
                new WGS84Coordinate(bottom, left), new WGS84Coordinate(top, right));
    }
}
