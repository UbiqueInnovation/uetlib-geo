package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.routing.utils.RoutingIdUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class CompleteGraphNeighbor<P> implements GraphNeighbor<P> {

    @JsonProperty
    private final long neighborId;

    @JsonProperty
    private final String neighborRoutingId;

    @JsonProperty
    private final P edgeProperties;

    public CompleteGraphNeighbor(long neighborId, String neighborRoutingId, P edgeProperties) {
        this.neighborId = neighborId;
        this.neighborRoutingId = neighborRoutingId;
        this.edgeProperties = edgeProperties;
    }

    public long neighborId() {
        return neighborId;
    }

    @Override
    public String neighborRoutingId() {
        return neighborRoutingId;
    }

    public P edgeProperties() {
        return edgeProperties;
    }

    public static <T> CompleteGraphNeighbor<T> fromEdge(long fromVertexId, Edge<Coordinate, T> edge) {
        final var neighborVertex = edge.getOther(fromVertexId);
        final var neighborRoutingId = RoutingIdUtil.getRoutingIdentifier(neighborVertex.getProperties());
        return new CompleteGraphNeighbor<>(neighborVertex.getId(), neighborRoutingId, edge.getProperties());
    }
}
