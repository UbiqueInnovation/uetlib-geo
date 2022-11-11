package ch.ubique.backend.geo.geojson.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * @deprecated This implementation does not use JTS geometries. Please use {@link
 *     ch.ubique.backend.geo.geojson.models.jts.FeatureCollection} instead. Will be removed with
 *     0.8.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection {

    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public FeatureCollection setFeatures(List<Feature> features) {
        this.features = features;
        return this;
    }

    @Override
    public String toString() {
        return "FeatureCollection(" + features.size() + ')';
    }
}
