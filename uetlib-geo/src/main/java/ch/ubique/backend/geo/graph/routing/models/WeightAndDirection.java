package ch.ubique.backend.geo.graph.routing.models;

import ch.ubique.backend.geo.graph.models.Vertex;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.locationtech.jts.geom.Coordinate;

public record WeightAndDirection(float weight, Vertex<Coordinate, Weight> direction) {

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("weight", weight)
                .append("direction", direction)
                .toString();
    }
}
