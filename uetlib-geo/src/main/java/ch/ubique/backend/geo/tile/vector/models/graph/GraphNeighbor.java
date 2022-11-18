package ch.ubique.backend.geo.tile.vector.models.graph;

public interface GraphNeighbor<P>{

    long neighborId();
    String neighborRoutingId();

    P edgeProperties();
}
