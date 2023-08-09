package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeatureCollection<T> {

    private static final String TYPE = "FeatureCollection";

    private Collection<T> features;

    public Collection<T> getFeatures() {
        return features;
    }

    public GenericFeatureCollection<T> setFeatures(final Collection<T> features) {
        this.features = features;
        return this;
    }

    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "FeatureCollection(" + features.size() + ')';
    }
}
