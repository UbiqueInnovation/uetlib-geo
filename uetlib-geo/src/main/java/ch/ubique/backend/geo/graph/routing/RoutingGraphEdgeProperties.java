package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Vertex;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.locationtech.jts.geom.Coordinate;

public record RoutingGraphEdgeProperties<E>(
        double distance,
        Vertex<Coordinate, E> direction,
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
