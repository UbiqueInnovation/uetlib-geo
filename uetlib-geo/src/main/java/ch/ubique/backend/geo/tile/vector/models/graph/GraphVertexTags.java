package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.routing.utils.RoutingIdUtil;
import org.locationtech.jts.geom.Coordinate;

public record GraphVertexTags(Double elevation, String routingId) {

    public GraphVertexTags(Coordinate coordinate) {
        this(coordinate.getZ() == Coordinate.NULL_ORDINATE ? null : coordinate.getZ(),
                RoutingIdUtil.getRoutingIdentifier(coordinate));
    }
}
