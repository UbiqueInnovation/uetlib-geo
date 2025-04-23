package ch.ubique.backend.geo.graph.routing.utils;

import static java.util.Objects.requireNonNull;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphVertexProperties;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;
import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile.VertexCoordinate;

import org.locationtech.jts.geom.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class RoutingGraphTileParser {

    private final RoutingGraphs graphs;
    private final Map<Integer, Coordinate> coordinates;

    public RoutingGraphTileParser() {
        this.graphs = new RoutingGraphs(new Graph<>(), new Graph<>());
        this.coordinates = new HashMap<>();
    }

    public RoutingGraphs graphs() {
        return graphs;
    }

    public RoutingGraphTileParser parse(final RoutingGraphTile tile) {
        for (var i = 0; i < tile.getCoordinatesCount(); i++) {
            final var coordinate = tile.getCoordinates(i);
            coordinates.computeIfAbsent(coordinate.getIdentifier(), id -> toJts(coordinate));
        }

        for (var i = 0; i < tile.getVerticesCount(); i++) {
            final var vertex = tile.getVertices(i);
            addToCompleteGraph(vertex);
        }

        for (var i = 0; i < tile.getVerticesCount(); i++) {
            final var vertex = tile.getVertices(i);
            addToRoutingGraph(vertex);
        }
        return this;
    }

    private void addToCompleteGraph(final RoutingGraphTile.Vertex vertex) {
        final var graph = graphs.completeGraph();
        for (var i = 0; i < vertex.getNeighborsCount(); i++) {
            final var neighbor = vertex.getNeighbors(i);
            graph.getOrAddDirectedEdge(
                    graph.getOrAddVertex(
                            vertex.getIdentifier(),
                            id -> createProperties(id, vertex.getIsHighway())),
                    graph.getOrAddVertex(
                            neighbor.getIdentifier(),
                            id -> createProperties(id, neighbor.getIsHighway())),
                    () -> new Weight(neighbor.getWeight()));
        }
    }

    private void addToRoutingGraph(final RoutingGraphTile.Vertex vertex) {
        final var graph = graphs.routingGraph();
        for (var i = 0; i < vertex.getRoutingNeighborsCount(); i++) {
            final var neighbor = vertex.getRoutingNeighbors(i);
            graph.getOrAddDirectedEdge(
                    graph.getOrAddVertex(
                            vertex.getIdentifier(),
                            id -> createProperties(id, vertex.getIsHighway())),
                    graph.getOrAddVertex(
                            neighbor.getIdentifier(),
                            id -> createProperties(id, neighbor.getIsHighway())),
                    () ->
                            new WeightAndDirection<>(
                                    neighbor.getWeight(),
                                    graphs.completeGraph()
                                            .getVertex(neighbor.getDirection())
                                            .orElseThrow()));
        }
    }

    private RoutingGraphVertexProperties createProperties(final int id, final boolean isHighway) {
        return new RoutingGraphVertexProperties(requireNonNull(coordinates.get(id)), isHighway);
    }

    private Coordinate toJts(final VertexCoordinate coordinate) {
        requireNonNull(coordinate);
        return new Coordinate(
                coordinate.getX(),
                coordinate.getY(),
                coordinate.hasElevation() ? coordinate.getElevation() : Coordinate.NULL_ORDINATE);
    }
}
