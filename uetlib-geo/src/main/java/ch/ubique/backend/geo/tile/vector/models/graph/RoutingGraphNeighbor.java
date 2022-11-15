package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class RoutingGraphNeighbor<P> implements GraphNeighbor<P> {

    @JsonProperty
    private final long neighborId;

    @JsonProperty
    private final long direction;

    @JsonProperty
    private final double distance;

    @JsonProperty
    private final P edgeProperties;

    public RoutingGraphNeighbor(long neighborId, long direction, double distance, P edgeProperties) {
        this.neighborId = neighborId;
        this.direction = direction;
        this.distance = distance;
        this.edgeProperties = edgeProperties;
    }

    public static <T> RoutingGraphNeighbor<T> fromEdge(
            long fromVertexId, Edge<Coordinate, RoutingGraphEdgeProperties<T>> edge) {

        final var properties = edge.getProperties();

        return new RoutingGraphNeighbor<>(
                edge.getOther(fromVertexId).getId(),
                properties.direction().getId(),
                properties.distance(),
                properties.originalProperties()
        );
    }

    @Override
    public long neighborId() {
        return neighborId;
    }

    public long direction() {
        return direction;
    }

    public double distance() {
        return distance;
    }

    @Override
    public P edgeProperties() {
        return edgeProperties;
    }
}
