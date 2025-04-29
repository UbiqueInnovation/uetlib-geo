package ch.ubique.backend.geo.graph.routing.models;

import ch.ubique.backend.geo.graph.models.Vertex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record WeightAndDirection<V, E>(float weight, @JsonIgnore Vertex<V, E> direction) {

    public WeightAndDirection<V, E> withDirection(final Vertex<V, E> direction) {
        return new WeightAndDirection<>(weight, direction);
    }

    @JsonProperty("direction")
    public int directionId() {
        return direction.getId();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("weight", weight)
                .append("direction", direction)
                .toString();
    }
}
