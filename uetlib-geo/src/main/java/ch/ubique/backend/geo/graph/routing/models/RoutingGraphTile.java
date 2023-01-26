package ch.ubique.backend.geo.graph.routing.models;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

public interface RoutingGraphTile {

    record Vertex(
            long id,
            @NotNull Point point,
            @NotNull Collection<RoutingGraphTile.Neighbor> neighbors,
            @NotNull Collection<RoutingGraphTile.RoutingNeighbor> routingNeighbors)
            implements VectorTileElement {

        @Override
        public String layer() {
            return "graph";
        }

        @Override
        public Geometry geometry() {
            return point;
        }

        public Coordinate coordinate() {
            return point.getCoordinate();
        }
    }

    record Neighbor(long id, @NotNull Coordinate coordinate, float weight) {}

    record RoutingNeighbor(long id, @NotNull Coordinate coordinate, float weight, long direction) {}
}
