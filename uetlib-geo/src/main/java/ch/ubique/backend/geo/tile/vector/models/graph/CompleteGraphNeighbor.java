package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import ch.ubique.backend.geo.graph.routing.RoutingLinkEdgeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class CompleteGraphNeighbor<P> implements GraphNeighbor<P> {

    @JsonProperty
    private final long neighborId;

    @JsonProperty
    private final long routingEdgeId;

    @JsonProperty
    private final P edgeProperties;

    public CompleteGraphNeighbor(long neighborId, long routingEdgeId, P edgeProperties) {
        this.neighborId = neighborId;
        this.routingEdgeId = routingEdgeId;
        this.edgeProperties = edgeProperties;
    }

    public long neighborId() {
        return neighborId;
    }

    public long routingEdgeId() {
        return routingEdgeId;
    }

    public P edgeProperties() {
        return edgeProperties;
    }

    public static <U extends Coordinate, S extends CoordinateVertexProperties<U>, T> CompleteGraphNeighbor<T> fromEdge(
            long fromVertexId,
            Edge<S, RoutingLinkEdgeProperties<T>> edge) {
        final var properties = edge.getProperties();

        return new CompleteGraphNeighbor<>(
                edge.getOther(fromVertexId).getId(),
                properties.getRoutingEdgeId(),
                properties.getOriginalProperties()
        );
    }
}
