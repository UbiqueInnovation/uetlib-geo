package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Point;

import java.util.Collection;

public interface GraphVertex<P> extends VectorTileElementWithMetadata<Collection<? extends GraphNeighbor<P>>> {
    @JsonIgnore
    Point point();
}
