package ch.ubique.uetlib.geo.tile.vector.mapbox.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

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
