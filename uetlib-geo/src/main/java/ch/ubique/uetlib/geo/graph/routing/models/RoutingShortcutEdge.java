package ch.ubique.uetlib.geo.graph.routing.models;

import ch.ubique.uetlib.geo.graph.models.Vertex;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record RoutingShortcutEdge(
        float weight, @JsonIgnore Vertex<RoutingVertex, RoutingCompleteEdge> direction)
        implements RoutingEdge {

    public RoutingShortcutEdge withDirection(
            final Vertex<RoutingVertex, RoutingCompleteEdge> direction) {
        return new RoutingShortcutEdge(weight, direction);
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
