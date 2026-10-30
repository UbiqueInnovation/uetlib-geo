package ch.ubique.uetlib.geo.graph.routing.utils;

import ch.ubique.uetlib.geo.graph.models.Edge;
import ch.ubique.uetlib.geo.graph.routing.RoutingGraphs;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingGraphTile;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingGraphTile.Neighbor;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingGraphTile.RoutingNeighbor;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.uetlib.geo.graph.routing.models.RoutingVertex;
import java.util.Collection;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.GeometryFactory;

public final class RoutingGraphsToTileElementsUtil {

    private RoutingGraphsToTileElementsUtil() {}

    public static Collection<RoutingGraphTile.Vertex> mapToTileElements(
            final RoutingGraphs graphs, final GeometryFactory geometryFactory) {

        return graphs.completeGraph().getVertices().stream()
                .map(vertex -> vertex(graphs, geometryFactory, vertex))
                .toList();
    }

    private static RoutingGraphTile.@NonNull Vertex vertex(
            final RoutingGraphs graphs,
            final GeometryFactory geometryFactory,
            final ch.ubique.uetlib.geo.graph.models.Vertex<RoutingVertex, RoutingCompleteEdge>
                    vertex) {
        final var routingVertex = graphs.routingGraph().getVertex(vertex.getId());
        return new RoutingGraphTile.Vertex(
                vertex.getId(),
                vertex.getProperties().isHighway(),
                geometryFactory.createPoint(vertex.getProperties().coordinate()),
                vertex.outgoingEdges().stream()
                        .map(RoutingGraphsToTileElementsUtil::mapToNeighbor)
                        .toList(),
                routingVertex.stream()
                        .flatMap(v -> v.outgoingEdges().stream())
                        .map(RoutingGraphsToTileElementsUtil::mapToRoutingNeighbor)
                        .toList());
    }

    private static RoutingGraphTile.@NonNull Neighbor mapToNeighbor(
            final Edge<RoutingVertex, RoutingCompleteEdge> edge) {
        final var to = edge.getTo();
        return new Neighbor(
                to.getId(),
                to.getProperties().isHighway(),
                to.getProperties().coordinate(),
                edge.getProperties().weight(),
                edge.getProperties().surfaceType());
    }

    private static RoutingGraphTile.@NonNull RoutingNeighbor mapToRoutingNeighbor(
            final Edge<RoutingVertex, RoutingShortcutEdge> edge) {
        return new RoutingNeighbor(
                edge.getTo().getId(),
                edge.getTo().getProperties().isHighway(),
                edge.getTo().getProperties().coordinate(),
                edge.getProperties().weight(),
                edge.getProperties().direction().getId());
    }
}
