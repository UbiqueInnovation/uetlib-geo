package ch.ubique.backend.geo.tile.vector.mapbox.models;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public record VectorTileFeature(
        long id, @NotNull Geometry geometry, @NotNull Map<String, Object> tags) {

    public VectorTileFeature(
            @NotNull final Geometry geometry, @NotNull final Map<String, Object> tags) {
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
