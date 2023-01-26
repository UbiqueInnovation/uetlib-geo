package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.Neighbor;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.RoutingNeighbor;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class RoutingGraphsToTileElementsUtil {

    private RoutingGraphsToTileElementsUtil() {}

    public static Collection<VectorTileElement> mapToTileElements(
            final RoutingGraphs graphs, final GeometryFactory geometryFactory) {

        return graphs.completeGraph().getVertices().stream()
                .map(vertex -> vertex(graphs, geometryFactory, vertex))
                .map(VectorTileElement.class::cast)
                .toList();
    }

    @NotNull
    private static RoutingGraphTile.Vertex vertex(
            final RoutingGraphs graphs,
            final GeometryFactory geometryFactory,
            final ch.ubique.backend.geo.graph.models.Vertex<Coordinate, Weight> vertex) {
        final var routingVertex = graphs.routingGraph().getVertex(vertex.getId());
        return new RoutingGraphTile.Vertex(
                vertex.getId(),
                geometryFactory.createPoint(vertex.getProperties()),
                vertex.getEdges().stream()
                        .map(RoutingGraphsToTileElementsUtil::mapToNeighbor)
                        .toList(),
                routingVertex.stream()
                        .flatMap(v -> v.getEdges().stream())
                        .map(RoutingGraphsToTileElementsUtil::mapToRoutingNeighbor)
                        .toList());
    }

    @NotNull
    private static RoutingGraphTile.Neighbor mapToNeighbor(final Edge<Coordinate, Weight> edge) {
        final var to = edge.getTo();
        return new Neighbor(to.getId(), to.getProperties(), edge.getProperties().weight());
    }

    @NotNull
    private static RoutingGraphTile.RoutingNeighbor mapToRoutingNeighbor(
            final Edge<Coordinate, WeightAndDirection> edge) {
        return new RoutingNeighbor(
                edge.getTo().getId(),
                edge.getTo().getProperties(),
                edge.getProperties().weight(),
                edge.getProperties().direction().getId());
    }
}
