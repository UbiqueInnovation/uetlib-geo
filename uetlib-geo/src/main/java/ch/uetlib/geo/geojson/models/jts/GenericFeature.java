package ch.uetlib.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;

/**
 * For an example on how to subclass this, check out the definition of {@link Feature}.
 *
 * @param <P> type of the {@link #properties}
 * @param <T> type of the subclass itself
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeature<P, T extends GenericFeature<P, T>> {

    @JsonInclude private @Nullable String id;

    @JsonInclude private String type = "Feature";

    @JsonInclude private @Nullable P properties;

    @JsonInclude private @Nullable Geometry geometry;

    public @Nullable String getId() {
        return id;
    }

    public T setId(final String id) {
        this.id = id;
        //noinspection unchecked
        return (T) this;
    }

    public @Nullable P getProperties() {
        return properties;
    }

    public T setProperties(final P properties) {
        this.properties = properties;
        //noinspection unchecked
        return (T) this;
    }

    public @Nullable Geometry getGeometry() {
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
}
