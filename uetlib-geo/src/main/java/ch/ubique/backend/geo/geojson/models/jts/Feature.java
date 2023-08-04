package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature extends GenericFeature<Map<String, Object>, Feature> {

    @JsonIgnore
    public <V> V getProperty(final String key) {
        //noinspection unchecked
        return (V) getProperties().get(key);
    }
}
