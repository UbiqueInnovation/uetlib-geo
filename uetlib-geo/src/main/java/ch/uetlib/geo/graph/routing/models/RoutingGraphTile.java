package ch.uetlib.geo.graph.routing.models;

import ch.uetlib.geo.graph.routing.protobuf.RoutingGraphTile.SurfaceType;
import ch.uetlib.geo.tile.vector.models.VectorTileElement;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.Collection;
import java.util.stream.Stream;

public interface RoutingGraphTile {

    record Vertex(
            long id,
            boolean isHighway,
            @NonNull Point point,
            @NonNull Collection<RoutingGraphTile.Neighbor> neighbors,
            @NonNull Collection<RoutingGraphTile.RoutingNeighbor> routingNeighbors)
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

        public Stream<Neighbor> neighborStream() {
            return neighbors.stream();
        }

        public Stream<RoutingNeighbor> routingNeighborStream() {
            return routingNeighbors.stream();
        }
    }

    record Neighbor(
            long id,
            boolean isHighway,
            @NonNull Coordinate coordinate,
            float weight,
            @Nullable SurfaceType surfaceType) {}

    record RoutingNeighbor(
            long id,
            boolean isHighway,
            @NonNull Coordinate coordinate,
            float weight,
            long direction) {}
}
