package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.RoutingCompleteEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingShortcutEdge;
import ch.ubique.backend.geo.graph.routing.models.RoutingVertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.LongUnaryOperator;

public final class RoutingGraphUtil {

    private static final Logger logger = LoggerFactory.getLogger(RoutingGraphUtil.class);

    private final Graph<RoutingVertex, RoutingCompleteEdge> inputGraph;
    private final Graph<RoutingVertex, RoutingShortcutEdge> routingGraph;
    private final Set<Vertex<RoutingVertex, RoutingCompleteEdge>> additionalRoutingVertices;

    private RoutingGraphUtil(
            final Graph<RoutingVertex, RoutingCompleteEdge> inputGraph, final long graphId) {
        this.inputGraph = inputGraph;
        routingGraph = new Graph<>(graphId);
        additionalRoutingVertices = new HashSet<>();
    }

    public static Graph<RoutingVertex, RoutingShortcutEdge> createRoutingGraph(
            final Graph<RoutingVertex, RoutingCompleteEdge> graph) {
        return createRoutingGraph(graph, LongUnaryOperator.identity());
    }

    public static Graph<RoutingVertex, RoutingShortcutEdge> createRoutingGraph(
            final Graph<RoutingVertex, RoutingCompleteEdge> graph,
            final LongUnaryOperator graphIdMapper) {
        final var routingGraphUtil =
                new RoutingGraphUtil(graph, graphIdMapper.applyAsLong(graph.getGraphId()));
        return routingGraphUtil.createRoutingGraph();
    }

    private Graph<RoutingVertex, RoutingShortcutEdge> createRoutingGraph() {
        logger.info("Creating routing graphs");

        logger.info("Finding cycles");
        final var cycles = inputGraph.findCycles();
        logger.info("Found {} cycles", cycles.size());
        for (final var cycle : cycles) {
            final var vertexOnCycle = cycle.getFirst();
            additionalRoutingVertices.add(vertexOnCycle);
        }

        final var routingVertices =
                inputGraph.getVertices().stream().filter(this::isRoutingVertex).toList();

        logger.info("Creating routing vertices");
        routingVertices.forEach(this::addToRoutingGraph);

        logger.info("Linking routing vertices");
        routingVertices.forEach(this::linkRoutingNode);

        logger.info("Successfully created routing graphs");
        return routingGraph;
    }

    private boolean isRoutingVertex(final Vertex<RoutingVertex, RoutingCompleteEdge> vertex) {
        return vertex.degree() == 1
                || vertex.degree() > 2
                || additionalRoutingVertices.contains(vertex);
    }

    private void addToRoutingGraph(final Vertex<RoutingVertex, RoutingCompleteEdge> vertex) {
        routingGraph.getOrAddVertex(vertex.getId(), vertex.getProperties());
    }

    private void linkRoutingNode(final Vertex<RoutingVertex, RoutingCompleteEdge> vertex) {
        for (final var direction : vertex.outgoingEdges()) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(
            final Vertex<RoutingVertex, RoutingCompleteEdge> start,
            final Edge<RoutingVertex, RoutingCompleteEdge> direction) {

        final var visitedEdges = new HashSet<Edge<RoutingVertex, RoutingCompleteEdge>>();

        var weight = 0.0f;

        var from = start;
        var current = direction;
        while (true) {
            visitedEdges.add(current);
            current.getTwin().ifPresent(visitedEdges::add);

            final var to = current.getOther(from);
            weight += current.getProperties().weight();

            // Link routing vertex and return
            if (isRoutingVertex(to)) {
                upsertRoutingEdge(start.getId(), to.getId(), direction.getOther(start), weight);
                return;
            }

            // Continue walking on path
            final var next =
                    to.outgoingEdges().stream()
                            .filter(edge -> !visitedEdges.contains(edge))
                            .findAny();

            assert next.isPresent();

            from = to;
            current = next.get();
        }
    }

    private void upsertRoutingEdge(
            final int vertexId,
            final int routingNeighborId,
            final Vertex<RoutingVertex, RoutingCompleteEdge> direction,
            final float weight) {

        // Try to get existing routing edge
        final var existingRoutingEdgeMaybe = routingGraph.getEdge(vertexId, routingNeighborId);

        if (existingRoutingEdgeMaybe.isPresent()) {
            final var existingRoutingEdge = existingRoutingEdgeMaybe.get();
            if (existingRoutingEdge.getProperties().weight() > weight) {
                // Found a shorter way, update routing properties
                final var routingProperties = new RoutingShortcutEdge(weight, direction);
                existingRoutingEdge.setProperties(routingProperties);
            }
            return;
        }

        // Insert new routing edge
        final var routingProperties = new RoutingShortcutEdge(weight, direction);
        if (vertexId != routingNeighborId) {
            routingGraph.getOrAddDirectedEdge(vertexId, routingNeighborId, routingProperties);
        }
    }
}
