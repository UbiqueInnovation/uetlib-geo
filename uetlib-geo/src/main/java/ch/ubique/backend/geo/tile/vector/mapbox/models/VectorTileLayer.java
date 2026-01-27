package ch.ubique.backend.geo.tile.vector.mapbox.models;

import static java.util.Objects.requireNonNullElseGet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

public record VectorTileLayer(
        @Positive int extent,
        @NotBlank String name,
        @NotNull Collection<VectorTileFeature> features) {

    public VectorTileLayer {
        if (extent <= 0) {
            throw new IllegalArgumentException("extent must be positive.");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        features = requireNonNullElseGet(features, List::of);
    }

    public VectorTileLayer withFeatures(final Collection<VectorTileFeature> features) {
        return new VectorTileLayer(extent, name, features);
    }
}
