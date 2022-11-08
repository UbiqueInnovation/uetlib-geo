package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record RoutingGraphEdgeProperties<V extends CoordinateVertexProperties, E>(
        double distance,
        Vertex<V, RoutingLinkEdgeProperties<E>> direction,
        E originalProperties) {

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("distance", distance)
                .append("direction", direction)
                .append("originalProperties", originalProperties)
                .toString();
    }
}
