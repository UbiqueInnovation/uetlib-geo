package ch.ubique.backend.geo.graph.routing.models;

import ch.ubique.backend.geo.graph.models.Vertex;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record WeightAndDirection<V, E>(float weight, Vertex<V, E> direction) {

    public WeightAndDirection<V, E> withDirection(final Vertex<V, E> direction) {
        return new WeightAndDirection<>(weight, direction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("weight", weight)
                .append("direction", direction)
                .toString();
    }
}
