package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;

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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("#features", features.size())
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final GenericFeatureCollection<?, ?, ?> that)) {
            return false;
        }

        return new EqualsBuilder().append(features, that.features).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(features).toHashCode();
    }
}
