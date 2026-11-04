package ch.uetlib.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jspecify.annotations.Nullable;

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

    private @Nullable Collection<F> features;

    public Collection<F> getFeatures() {
        if (features == null) {
            return List.of();
        }
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("#features", getFeatures().size())
                .toString();
    }
}
