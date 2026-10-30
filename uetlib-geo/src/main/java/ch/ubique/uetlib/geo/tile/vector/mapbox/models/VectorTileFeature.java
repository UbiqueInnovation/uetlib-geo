package ch.ubique.uetlib.geo.tile.vector.mapbox.models;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Geometry;

public record VectorTileFeature(
        long id, @NonNull Geometry geometry, @NonNull Map<String, Object> tags) {

    public VectorTileFeature(
            @NonNull final Geometry geometry, @NonNull final Map<String, Object> tags) {
        this(0, geometry, tags);
    }

    public VectorTileFeature {
        requireNonNull(geometry);
        tags = requireNonNullElseGet(tags, Map::of);
    }

    public VectorTileFeature withTags(final Map<String, Object> tags) {
        return new VectorTileFeature(id, geometry, tags);
    }
}
