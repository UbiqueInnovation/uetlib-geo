package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.models.Weight;
import ch.ubique.backend.geo.graph.routing.models.WeightAndDirection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingGraphUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static Graph<Coordinate, WeightAndDirection> createRoutingGraph(
            final Graph<Coordinate, Weight> graph) {
        final var routingGraphUtil = new RoutingGraphUtil(graph);
        return routingGraphUtil.createRoutingGraph();
    }

    private final Graph<Coordinate, Weight> inputGraph;
    private final Graph<Coordinate, WeightAndDirection> routingGraph;
    private final Set<Vertex<Coordinate, Weight>> additionalRoutingVertices;

    private RoutingGraphUtil(final Graph<Coordinate, Weight> inputGraph) {
        this.inputGraph = inputGraph;
        routingGraph = new Graph<>();
        additionalRoutingVertices = new HashSet<>();
    }

    private Graph<Coordinate, WeightAndDirection> createRoutingGraph() {
        logger.info("Creating routing graphs");

        logger.info("Finding cycles");
        var cycles = inputGraph.findCycles();
        logger.info("Found {} cycles", cycles.size());
        for (var cycle : cycles) {
            final var vertexOnCycle = cycle.get(0);
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

    private boolean isRoutingVertex(Vertex<Coordinate, Weight> vertex) {
        return vertex.degree() == 1
                || vertex.degree() > 2
                || additionalRoutingVertices.contains(vertex);
    }

    private void addToRoutingGraph(Vertex<Coordinate, Weight> vertex) {
        routingGraph.getOrAddVertex(new Vertex<>(vertex.getId(), vertex.getProperties()));
    }

    private void linkRoutingNode(Vertex<Coordinate, Weight> vertex) {
        for (final var direction : vertex.getEdges()) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(
            Vertex<Coordinate, Weight> start, Edge<Coordinate, Weight> direction) {

        final var visitedEdges = new HashSet<Edge<Coordinate, Weight>>();

        var weight = 0.0f;

        var from = start;
        var current = direction;
        while (true) {
            visitedEdges.add(current);
            final var to = current.getOther(from);
            weight += Optional.ofNullable(current.getProperties()).map(Weight::weight).orElse(0f);

            // Link routing vertex and return
            if (isRoutingVertex(to)) {
                upsertRoutingEdge(start.getId(), to.getId(), direction.getOther(start), weight);
                return;
            }

            // Continue walking on path
            final var next =
                    to.getEdges().stream().filter(edge -> !visitedEdges.contains(edge)).findAny();

            assert next.isPresent();

            from = to;
            current = next.get();
        }
    }

    private void upsertRoutingEdge(
            long vertexId,
            long routingNeighborId,
            Vertex<Coordinate, Weight> direction,
            float weight) {

        // Try to get existing routing edge
        final var existingRoutingEdgeMaybe = routingGraph.getEdge(vertexId, routingNeighborId);

        if (existingRoutingEdgeMaybe.isPresent()) {
            final var existingRoutingEdge = existingRoutingEdgeMaybe.get();
            if (existingRoutingEdge.getProperties().weight() > weight) {
                // Found a shorter way, update routing properties
                final var routingProperties = new WeightAndDirection(weight, direction);
                existingRoutingEdge.setProperties(routingProperties);
            }
            return;
        }

        // Insert new routing edge
        final var routingProperties = new WeightAndDirection(weight, direction);
        if (vertexId != routingNeighborId) {
            routingGraph.getOrAddDirectedEdge(vertexId, routingNeighborId, routingProperties);
        }
    }
}
