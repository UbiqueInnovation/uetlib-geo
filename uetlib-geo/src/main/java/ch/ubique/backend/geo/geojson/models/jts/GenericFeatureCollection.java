package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.List;

/**
 * For an example on how to subclass this, check out the definition of {@link FeatureCollection}.
 *
 * @param <F> type of the features {@link #features}
 * @param <P> type of the properties of the features
 * @param <T> type of the subclass itself
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeatureCollection<
        F extends GenericFeature<P, F>, P, T extends GenericFeatureCollection<F, P, T>> {

    private static final String TYPE = "FeatureCollection";

    private Collection<F> features;

    public Collection<F> getFeatures() {
        return features;
    }

    public T setFeatures(final Collection<F> features) {
        this.features = features;
        //noinspection unchecked
        return (T) this;
    }

    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "FeatureCollection(" + features.size() + ')';
    }

    public T withNewFeatures(final List<F> transformedFeatures) {
        return new GenericFeatureCollection<F, P, T>().setFeatures(transformedFeatures);
    }
}
