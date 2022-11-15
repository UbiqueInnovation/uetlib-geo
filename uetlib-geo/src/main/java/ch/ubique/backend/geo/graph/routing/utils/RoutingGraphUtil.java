package ch.ubique.backend.geo.graph.routing.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.routing.RoutingGraphEdgeProperties;
import org.eclipse.emf.common.util.WrappedException;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RoutingGraphUtil<E> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CoordinateReferenceSystem crs;

    public static <T> Graph<Coordinate, RoutingGraphEdgeProperties<T>> createRoutingGraph(
            final Graph<Coordinate, T> graph, final CoordinateReferenceSystem crs) {
        final var routingGraphUtil = new RoutingGraphUtil<>(graph, crs);
        return routingGraphUtil.createRoutingGraph();
    }

    private final Graph<Coordinate, E> inputGraph;
    private final Graph<Coordinate, RoutingGraphEdgeProperties<E>> routingGraph;
    private final Set<Vertex<Coordinate, E>> additionalRoutingVertices;

    private RoutingGraphUtil(final Graph<Coordinate, E> inputGraph, final CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.inputGraph = inputGraph;
        routingGraph = new Graph<>();
        additionalRoutingVertices = new HashSet<>();
    }

    private Graph<Coordinate, RoutingGraphEdgeProperties<E>> createRoutingGraph() {
        logger.info("Creating routing graphs");

        logger.info("Finding cycles");
        var cycles = inputGraph.findCycles();
        logger.info("Found {} cycles", cycles.size());
        for (var cycle : cycles) {
            final var vertexOnCycle = cycle.get(0);
            additionalRoutingVertices.add(vertexOnCycle);
        }

        final var routingVertices = inputGraph.getVertices()
                .stream()
                .filter(this::isRoutingVertex)
                .toList();

        logger.info("Creating routing vertices");
        routingVertices.forEach(this::addToRoutingGraph);

        logger.info("Linking routing vertices");
        routingVertices.forEach(this::linkRoutingNode);

        logger.info("Successfully created routing graphs");
        return routingGraph;
    }

    private boolean isRoutingVertex(Vertex<Coordinate, E> vertex) {
        return vertex.degree() == 1 || vertex.degree() > 2 || additionalRoutingVertices.contains(vertex);
    }

    private void addToRoutingGraph(Vertex<Coordinate, E> vertex) {
        routingGraph.getOrAddVertex(new Vertex<>(vertex.getId(), vertex.getProperties()));
    }

    private void linkRoutingNode(Vertex<Coordinate, E> vertex) {
        for (final var direction : vertex.getEdges()) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(
            Vertex<Coordinate, E> start,
            Edge<Coordinate, E>  direction) {

        final var visitedEdges = new HashSet<Edge<Coordinate, E>>();

        var distance = 0.0;

        var from = start;
        var current = direction;
        while (true) {
            visitedEdges.add(current);
            final var to = current.getOther(from);
            distance += getDistance(from, to);

            // Link routing vertex and return
            if (isRoutingVertex(to)) {
                upsertRoutingEdge(start.getId(), to.getId(), direction.getOther(start), distance, direction.getProperties());
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
            Vertex<Coordinate, E> direction,
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
            return;
        }

        // Insert new routing edge
        final var routingProperties = new RoutingGraphEdgeProperties<>(distance, direction, originalProperties);
        routingGraph.getOrAddEdge(vertexId, routingNeighborId, routingProperties);
    }

    private double getDistance(Vertex<Coordinate, E> from, Vertex<Coordinate, E> to) {
        try {
            return JTS.orthodromicDistance(from.getProperties(), to.getProperties(), crs);
        } catch (TransformException e) {
            throw new WrappedException("Could not compute distance.", e);
        }
    }
}
