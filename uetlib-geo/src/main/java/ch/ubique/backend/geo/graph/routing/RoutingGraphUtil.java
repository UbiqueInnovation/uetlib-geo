package ch.ubique.backend.geo.graph.routing;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RoutingGraphUtil<V extends CoordinateVertexProperties, E> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static <S extends CoordinateVertexProperties, T> RoutingGraphs<S, T> createRoutingGraph(
            Graph<S, T> graph) {
        final var routingGraphUtil = new RoutingGraphUtil<>(graph);
        return routingGraphUtil.createRoutingGraph();
    }

    private final Graph<V, RoutingLinkEdgeProperties<E>> completeGraph;
    private final Graph<V, RoutingGraphEdgeProperties<V, E>> routingGraph;

    private final Set<Vertex<V, RoutingLinkEdgeProperties<E>>> additionalRoutingVertices;

    private RoutingGraphUtil(Graph<V, E> inputGraph) {
        routingGraph = new Graph<>();
        completeGraph = createCompleteGraph(inputGraph);
        additionalRoutingVertices = new HashSet<>();
    }

    private RoutingGraphs<V, E> createRoutingGraph() {
        logger.info("Creating routing graphs");

        logger.info("Finding cycles");
        var cycles = completeGraph.findCycles();
        logger.info("Found {} cycles", cycles.size());
        for (var cycle : cycles) {
            final var vertexOnCycle = cycle.get(0);
            additionalRoutingVertices.add(vertexOnCycle);
        }

        final var routingVertices = completeGraph.getVertices()
                .stream()
                .filter(this::isRoutingVertex)
                .toList();

        logger.info("Creating routing vertices");
        routingVertices.forEach(this::addToRoutingGraph);

        logger.info("Linking routing vertices");
        routingVertices.forEach(this::linkRoutingNode);

        logger.info("Successfully created routing graphs");
        return new RoutingGraphs<>(completeGraph, routingGraph);
    }

    private Graph<V, RoutingLinkEdgeProperties<E>> createCompleteGraph(Graph<V, E> inputGraph) {
        final var graph = new Graph<V, RoutingLinkEdgeProperties<E>>();
        inputGraph.getVertices().forEach(v -> graph.addVertex(new Vertex<>(v.getId(), v.getProperties())));
        inputGraph.getEdges().forEach(e -> graph.getOrAddEdge(e.getU().getId(), e.getV().getId(),
                new RoutingLinkEdgeProperties<>(e.getProperties())));
        return graph;
    }

    private boolean isRoutingVertex(Vertex<V, RoutingLinkEdgeProperties<E>> vertex) {
        return vertex.degree() == 1 || vertex.degree() > 2 || additionalRoutingVertices.contains(vertex);
    }

    private void addToRoutingGraph(Vertex<V, RoutingLinkEdgeProperties<E>> vertex) {
        routingGraph.getOrAddVertex(new Vertex<>(vertex.getId(), vertex.getProperties()));
    }

    private void linkRoutingNode(Vertex<V, RoutingLinkEdgeProperties<E>> vertex) {
        for (final var direction : vertex.getEdges()) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(
            Vertex<V, RoutingLinkEdgeProperties<E>> start,
            Edge<V, RoutingLinkEdgeProperties<E>> direction) {

        final var visitedEdges = new HashSet<Edge<V, RoutingLinkEdgeProperties<E>>>();

        var distance = 0.0;

        var from = start;
        var current = direction;
        while (true) {
            visitedEdges.add(current);
            final var to = current.getOther(from);
            distance += from.getProperties().coordinate().distanceTo(to.getProperties().coordinate());

            // Link routing vertex and return
            if (isRoutingVertex(to)) {
                final var routingEdge = upsertRoutingEdge(start.getId(), to.getId(), direction.getOther(start), distance, direction.getProperties().getOriginalProperties());
                for (final var edgeOnPath : visitedEdges) {
                    edgeOnPath.getProperties().setRoutingEdgeId(routingEdge.getId());
                }
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

    private Edge<V, RoutingGraphEdgeProperties<V, E>> upsertRoutingEdge(
            long vertexId,
            long routingNeighborId,
            Vertex<V, RoutingLinkEdgeProperties<E>> direction,
            double distance, E originalProperties) {

        // Try to get existing routing edge
        final var existingRoutingEdgeMaybe = routingGraph.getVertex(vertexId).getEdgeTo(routingNeighborId);

        if (existingRoutingEdgeMaybe.isPresent()) {
            final var existingRoutingEdge = existingRoutingEdgeMaybe.get();
            if (existingRoutingEdge.getProperties().distance() > distance) {
                // Found a shorter way, update routing properties
                final var routingProperties = new RoutingGraphEdgeProperties<>(distance, direction, existingRoutingEdge.getProperties().originalProperties());
                existingRoutingEdge.setProperties(routingProperties);
            }
            return existingRoutingEdge;
        }

        // Insert new routing edge
        final var routingProperties = new RoutingGraphEdgeProperties<>(distance, direction, originalProperties);
        return routingGraph.getOrAddEdge(vertexId, routingNeighborId, routingProperties);
    }
}
