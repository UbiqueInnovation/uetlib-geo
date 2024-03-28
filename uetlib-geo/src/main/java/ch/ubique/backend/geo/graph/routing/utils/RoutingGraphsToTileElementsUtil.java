package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.Neighbor;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.RoutingNeighbor;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.GeometryFactory;

import java.util.Collection;

public class RoutingGraphsToTileElementsUtil {

    private RoutingGraphsToTileElementsUtil() {}

    public static Collection<RoutingGraphTile.Vertex> mapToTileElements(
            final RoutingGraphs graphs, final GeometryFactory geometryFactory) {

        return graphs.completeGraph().getVertices().stream()
                .map(vertex -> vertex(graphs, geometryFactory, vertex))
                .toList();
    }

    @NotNull
    private static RoutingGraphTile.Vertex vertex(
            final RoutingGraphs graphs,
            final GeometryFactory geometryFactory,
            final ch.ubique.backend.geo.graph.models.Vertex<RoutingGraphVertexProperties, Weight>
                    vertex) {
        final var routingVertex = graphs.routingGraph().getVertex(vertex.getId());
        return new RoutingGraphTile.Vertex(
                vertex.getId(),
                vertex.getProperties().isHighway(),
                geometryFactory.createPoint(vertex.getProperties().coordinate()),
                vertex.getEdges().stream()
                        .map(RoutingGraphsToTileElementsUtil::mapToNeighbor)
                        .toList(),
                routingVertex.stream()
                        .flatMap(v -> v.getEdges().stream())
                        .map(RoutingGraphsToTileElementsUtil::mapToRoutingNeighbor)
                        .toList());
    }

    @NotNull
    private static RoutingGraphTile.Neighbor mapToNeighbor(
            final Edge<RoutingGraphVertexProperties, Weight> edge) {
        final var to = edge.getTo();
        return new Neighbor(
                to.getId(),
                to.getProperties().isHighway(),
                to.getProperties().coordinate(),
                edge.getProperties().weight());
    }

    @NotNull
    private static RoutingGraphTile.RoutingNeighbor mapToRoutingNeighbor(
            final Edge<
                            RoutingGraphVertexProperties,
                            WeightAndDirection<RoutingGraphVertexProperties>>
                    edge) {
        return new RoutingNeighbor(
                edge.getTo().getId(),
                edge.getTo().getProperties().isHighway(),
                edge.getTo().getProperties().coordinate(),
                edge.getProperties().weight(),
                edge.getProperties().direction().getId());
    }
}
