package ch.uetlib.geo.tile.vector.mapbox.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public record VectorTileLayer(
        @Positive int extent, @NotBlank String name, Collection<VectorTileFeature> features) {

    public VectorTileLayer {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name must not be blank.");
        }
    }

    public VectorTileLayer withFeatures(final Collection<VectorTileFeature> features) {
        return new VectorTileLayer(extent, name, features);
    }
}
