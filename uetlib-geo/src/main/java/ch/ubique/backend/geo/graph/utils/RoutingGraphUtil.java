package ch.ubique.backend.geo.graph.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.EdgeProperties;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RoutingGraphUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    public static Graph createRoutingGraph(Graph graph) {
        RoutingGraphUtil routingGraphUtil = new RoutingGraphUtil(graph);
        routingGraphUtil.createRoutingGraph();
        return routingGraphUtil.routingGraph;
    }

    private final Graph graph;
    private final Graph routingGraph;

    private final Set<Vertex> additionalRoutingVertices;

    private RoutingGraphUtil(Graph graph) {
        this.graph = graph;
        routingGraph = new Graph();
        additionalRoutingVertices = new HashSet<>();
    }

    private boolean isRoutingVertex(Vertex vertex) {
        return vertex.degree() == 1
                || vertex.degree() > 2
                || additionalRoutingVertices.contains(vertex);
    }

    private void createRoutingGraph() {
        logger.info("Creating routing graph");

        var cycles = findCycles();
        logger.info("Found {} cycles", cycles.size());
        for (var cycle : cycles) {
            additionalRoutingVertices.add(cycle.get(0));
        }

        var routingVertices =
                graph.getVertices().stream()
                        .filter(this::isRoutingVertex)
                        .collect(Collectors.toList());

        logger.info("Creating routing vertices");
        routingVertices.forEach(vertex -> routingGraph.addVertex(vertex.copyWithoutEdges()));

        logger.info("Linking routing vertices");
        routingVertices.forEach(this::linkRoutingNode);

        logger.info("Successfully created routing graph");
    }

    private void linkRoutingNode(Vertex vertex) {
        var edges = new ArrayList<>(vertex.getEdges());
        for (Edge direction : edges) {
            linkRoutingNodeForNeighbor(vertex, direction);
        }
    }

    private void linkRoutingNodeForNeighbor(Vertex vertex, Edge direction) {

        Set<Edge> visitedEdges = new HashSet<>();
        visitedEdges.add(direction);

        Vertex directionVertex = direction.getOther(vertex);
        double distance = 0;

        Vertex from = vertex;
        Edge current = direction;
        while (true) {
            visitedEdges.add(current);
            Vertex to = current.getOther(from);
            distance += from.distanceTo(to);

            // Link routing vertex and break
            if (isRoutingVertex(to)) {
                Edge routingEdge = upsertRoutingNodeNeighbor(vertex, to, directionVertex, distance);
                for (Edge edgeOnPath : visitedEdges) {
                    if (edgeOnPath.getProperties() == null) {
                        edgeOnPath.setProperties(new EdgeProperties());
                    }
                    edgeOnPath.getProperties().setRoutingEdgeId(routingEdge.getId());
                }
                break;
            }

            // Continue walking on path
            Optional<Edge> unvisitedEdge =
                    to.getEdges().stream().filter(edge -> !visitedEdges.contains(edge)).findAny();
            assert unvisitedEdge.isPresent();

            Edge next = unvisitedEdge.get();
            from = to;
            current = next;
        }
    }

    private Edge upsertRoutingNodeNeighbor(
            Vertex vertex, Vertex routingNeighbor, Vertex direction, double distance) {
        boolean foundExisting = false;

        Vertex routingVertex = routingGraph.getVertex(vertex.getId());
        routingVertex.setProperties(vertex.getProperties());

        Edge routingEdge = null;

        // Update existing routing neighbor if the new path is shorter
        for (Edge existingRoutingEdge : routingVertex.getEdges()) {
            if (existingRoutingEdge.getV().equals(routingNeighbor)
                    && existingRoutingEdge.getProperties().getDistance() > distance) {
                foundExisting = true;
                routingEdge = existingRoutingEdge;
                routingEdge.getProperties().setDistance(distance);
                routingEdge.getProperties().setDirection(direction);
            }
        }

        // Insert new routing neighbor if needed
        if (!foundExisting) {
            EdgeProperties properties = new EdgeProperties();
            properties.setDistance(distance);
            properties.setDirection(direction);
            routingEdge = routingGraph.addEdge(vertex.getId(), routingNeighbor.getId(), properties);
        }

        return routingEdge;
    }

    private Set<List<Vertex>> findCycles() {

        Set<Vertex> visited = new HashSet<>();
        Set<List<Vertex>> cycles = new HashSet<>();

        for (var start : graph.getVertices()) {

            if (visited.contains(start) || start.degree() != 2) {
                continue;
            }

            Vertex previous = null;
            Vertex current = start;
            List<Vertex> potentialCycle = new LinkedList<>();
            while (current.degree() == 2) {

                if (current.equals(start) && !potentialCycle.isEmpty()) {
                    cycles.add(potentialCycle);
                    break;
                }

                visited.add(current);
                potentialCycle.add(current);

                final Vertex prev = previous;
                Optional<Vertex> unvisitedVertex =
                        current.getNeighbors().stream().filter(v -> !v.equals(prev)).findAny();

                assert unvisitedVertex.isPresent();

                Vertex next = unvisitedVertex.get();
                previous = current;
                current = next;
            }
        }
        return cycles;
    }
}
