package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection {

    private static final String type = "FeatureCollection";

    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public FeatureCollection setFeatures(final List<Feature> features) {
        this.features = features;
        return this;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "FeatureCollection(" + features.size() + ')';
    }
}
