package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.locationtech.jts.geom.Geometry;

/**
 * For an example on how to subclass this, check out the definition of {@link Feature}.
 *
 * @param <P> type of the {@link #properties}
 * @param <T> type of the subclass itself
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeature<P, T extends GenericFeature<P, T>> {

    @JsonInclude private String id;

    @JsonInclude private String type = "Feature";

    @JsonInclude private P properties;

    @JsonInclude private Geometry geometry;

    public String getId() {
        return id;
    }

    public T setId(final String id) {
        this.id = id;
        //noinspection unchecked
        return (T) this;
    }

    public P getProperties() {
        return properties;
    }

    public T setProperties(final P properties) {
        this.properties = properties;
        //noinspection unchecked
        return (T) this;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    @DeserializableJtsGeometry
    public T setGeometry(final Geometry geometry) {
        this.geometry = geometry;
        //noinspection unchecked
        return (T) this;
    }

    public String getType() {
        return type;
    }

    public T setType(final String type) {
        this.type = type;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("type", type)
                .append("properties", properties)
                .append("geometry", geometry)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof final GenericFeature<?, ?> that)) return false;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(type, that.type)
                .append(properties, that.properties)
                .append(geometry, that.geometry)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(type)
                .append(properties)
                .append(geometry)
                .toHashCode();
    }
}
