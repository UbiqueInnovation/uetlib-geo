package ch.ubique.backend.geo.tile.vector.models.graph;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTagsAndMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Point;

import java.util.Collection;

public interface GraphVertex<P> extends VectorTileElementWithTagsAndMetadata<GraphVertexTags, Collection<? extends GraphNeighbor<P>>> {
    @JsonIgnore
    Point point();
}
