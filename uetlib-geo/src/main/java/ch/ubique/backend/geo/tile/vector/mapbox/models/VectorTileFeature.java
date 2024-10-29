package ch.ubique.backend.geo.tile.vector.mapbox.models;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;
import java.util.Objects;

public record VectorTileFeature(
        long id, @NotNull Geometry geometry, @NotNull Map<String, Object> tags) {

    public VectorTileFeature {
        Objects.requireNonNull(geometry);
        tags = Objects.requireNonNullElseGet(tags, Map::of);
    }

    public VectorTileFeature withTags(final Map<String, Object> tags) {
        return new VectorTileFeature(id, geometry, tags);
    }
}
