package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementUnweightedAlwaysVisible;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public interface GraphVertex<C extends Coordinate, P> extends VectorTileElementUnweightedAlwaysVisible<ObjectUtils.Null, Collection<? extends GraphNeighbor<P>>> {
    long id();
    C coordinate();
}
