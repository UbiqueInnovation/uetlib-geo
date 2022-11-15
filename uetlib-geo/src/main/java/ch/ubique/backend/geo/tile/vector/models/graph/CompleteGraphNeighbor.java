package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.graph.models.Edge;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheritance
public class CompleteGraphNeighbor<P> implements GraphNeighbor<P> {

    @JsonProperty
    private final long neighborId;

    @JsonProperty
    private final P edgeProperties;

    public CompleteGraphNeighbor(long neighborId, P edgeProperties) {
        this.neighborId = neighborId;
        this.edgeProperties = edgeProperties;
    }

    public long neighborId() {
        return neighborId;
    }

    public P edgeProperties() {
        return edgeProperties;
    }

    public static <T> CompleteGraphNeighbor<T> fromEdge(long fromVertexId, Edge<Coordinate, T> edge) {

        return new CompleteGraphNeighbor<>(edge.getOther(fromVertexId).getId(), edge.getProperties());
    }
}
