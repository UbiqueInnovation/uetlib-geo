package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;

public final class RoutingGraphUtil<V, E> {

    private static final Logger logger = LoggerFactory.getLogger(RoutingGraphUtil.class);

    private final Graph<V, E> inputGraph;
    private final Graph<V, WeightAndDirection<V, E>> routingGraph;
    private final Set<Vertex<V, E>> additionalRoutingVertices;
    private final Function<Edge<V, E>, Float> weightingFunction;

    private RoutingGraphUtil(
            final Graph<V, E> inputGraph,
            final Function<Edge<V, E>, Float> weightingFunction,
            final long graphId) {
        this.inputGraph = inputGraph;
        this.weightingFunction = weightingFunction;
        routingGraph = new Graph<>(graphId);
        additionalRoutingVertices = new HashSet<>();
    }

    public static <S, T> Graph<S, WeightAndDirection<S, T>> createRoutingGraph(
            final Graph<S, T> graph, final Function<Edge<S, T>, Float> weightingFunction) {
        return createRoutingGraph(graph, weightingFunction, LongUnaryOperator.identity());
    }

    public static <S> Graph<S, WeightAndDirection<S, Weight>> createRoutingGraph(
            final Graph<S, Weight> graph) {
        return createRoutingGraph(
                graph,
                e -> Optional.ofNullable(e.getProperties()).map(Weight::weight).orElse(null),
                LongUnaryOperator.identity());
    }

    public static <S, T> Graph<S, WeightAndDirection<S, T>> createRoutingGraph(
            final Graph<S, T> graph,
            final Function<Edge<S, T>, Float> weightingFunction,
            final LongUnaryOperator graphIdMapper) {
        final var routingGraphUtil =
                new RoutingGraphUtil<>(
                        graph, weightingFunction, graphIdMapper.applyAsLong(graph.getGraphId()));
        return routingGraphUtil.createRoutingGraph();
    }

    private Graph<V, WeightAndDirection<V, E>> createRoutingGraph() {
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

    private boolean isRoutingVertex(final Vertex<V, E> vertex) {
        return vertex.degree() == 1
                || vertex.degree() > 2
                || additionalRoutingVertices.contains(vertex);
    }

    private void addToRoutingGraph(final Vertex<V, E> vertex) {
        routingGraph.getOrAddVertex(vertex.getId(), vertex.getProperties());
    }

    private void linkRoutingNode(final Vertex<V, E> vertex) {
        for (final var direction : vertex.outgoingEdges()) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(final Vertex<V, E> start, final Edge<V, E> direction) {

        final var visitedEdges = new HashSet<Edge<V, E>>();

        var weight = 0.0f;

        var from = start;
        var current = direction;
        while (true) {
            visitedEdges.add(current);
            current.getTwin().ifPresent(visitedEdges::add);

            final var to = current.getOther(from);
            weight += Optional.of(current).map(weightingFunction).orElse(0.0f);

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
            final Vertex<V, E> direction,
            final float weight) {

        // Try to get existing routing edge
        final var existingRoutingEdgeMaybe = routingGraph.getEdge(vertexId, routingNeighborId);

        if (existingRoutingEdgeMaybe.isPresent()) {
            final var existingRoutingEdge = existingRoutingEdgeMaybe.get();
            if (existingRoutingEdge.getProperties().weight() > weight) {
                // Found a shorter way, update routing properties
                final var routingProperties = new WeightAndDirection<>(weight, direction);
                existingRoutingEdge.setProperties(routingProperties);
            }
            return;
        }

        // Insert new routing edge
        final var routingProperties = new WeightAndDirection<>(weight, direction);
        if (vertexId != routingNeighborId) {
            routingGraph.getOrAddDirectedEdge(vertexId, routingNeighborId, routingProperties);
        }
    }
}
