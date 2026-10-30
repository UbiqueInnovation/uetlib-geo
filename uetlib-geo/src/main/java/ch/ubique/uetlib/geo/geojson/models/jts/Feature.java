package ch.ubique.uetlib.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import org.jspecify.annotations.Nullable;

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
