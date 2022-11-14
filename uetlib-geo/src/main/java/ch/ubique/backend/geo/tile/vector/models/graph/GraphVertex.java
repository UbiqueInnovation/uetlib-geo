package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithMetadata;

import java.util.Collection;

public interface GraphVertex<C extends Coordinate, P> extends VectorTileElementWithMetadata<Collection<? extends GraphNeighbor<P>>> {
    long id();
    C coordinate();
}
