package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.jackson;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;

public class TileJsonBoundsSerializer extends JsonSerializer<BoundingBox<WGS84Coordinate>> {

    @Override
    public void serialize(
            final BoundingBox<WGS84Coordinate> value,
            final JsonGenerator gen,
            final SerializerProvider serializers)
            throws IOException {
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
