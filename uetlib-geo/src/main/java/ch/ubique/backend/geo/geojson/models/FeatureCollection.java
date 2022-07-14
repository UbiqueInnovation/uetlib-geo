package ch.ubique.backend.geo.geojson.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

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
