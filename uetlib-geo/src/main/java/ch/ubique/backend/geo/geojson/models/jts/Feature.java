package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jspecify.annotations.Nullable;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature extends GenericFeature<Map<String, Object>, Feature> {

    @JsonIgnore
    public <V> @Nullable V getProperty(final String key) {
        if (getProperties() == null) {
            return null;
        }
        //noinspection unchecked
        return (V) getProperties().get(key);
    }
}
