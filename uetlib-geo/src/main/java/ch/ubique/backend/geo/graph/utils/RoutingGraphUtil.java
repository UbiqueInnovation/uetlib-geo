package ch.ubique.backend.geo.graph.utils;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.EdgeProperties;
import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.models.Vertex;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RoutingGraphUtil {


    public static Graph createRoutingGraph(Graph graph) {
        RoutingGraphUtil routingGraphUtil = new RoutingGraphUtil(graph);
        routingGraphUtil.createRoutingGraph();
        return routingGraphUtil.routingGraph;
    }


    private final Graph graph;
    private final Graph routingGraph;

    private RoutingGraphUtil(Graph graph) {
        this.graph = graph;
        this.routingGraph = new Graph();
    }

    private void createRoutingGraph() {
        graph.getVertices().stream()
            .filter(vertex -> vertex.degree() > 2)
            .peek(vertex -> routingGraph.addVertex(vertex.copyWithoutEdges()))
            .forEach(this::linkRoutingNode);
    }

    private void linkRoutingNode(Vertex vertex) {
        for (Vertex neighbor : vertex.getNeighbors()) {
            linkRoutingNodeForNeighbor(vertex, neighbor);
        }
    }

    private void linkRoutingNodeForNeighbor(Vertex vertex, Vertex neighbor) {

        Set<Vertex> visited = new HashSet<>();
        visited.add(vertex);

        double distance = vertex.distanceTo(neighbor);

        Vertex current = neighbor;
        while (current != null) {
            visited.add(current);

            // Is part of an intersection
            if (current.degree() > 2) {
                upsertRoutingNodeNeighbor(vertex, current, neighbor, distance);
                break;
            }

            // Continue walking on path
            Optional<Vertex> unvisitedNeighbor = current.getNeighbors().stream().filter(n -> !visited.contains(n)).findAny();
            if (unvisitedNeighbor.isPresent()) {
                Vertex next = unvisitedNeighbor.get();
                distance += current.distanceTo(next);
                current = next;
            } else {
                current = null;
            }
        }
    }

    private void upsertRoutingNodeNeighbor(Vertex vertex, Vertex routingNeighbor, Vertex direction, double distance) {
        boolean foundExisting = false;

        Vertex routingVertex = routingGraph.getVertex(vertex.getId());

        // Update existing routing neighbor if the new path is shorter
        for (Edge existingRoutingEdge : routingVertex.getEdges()) {
            if (existingRoutingEdge.getV().equals(routingNeighbor)) {
                foundExisting = true;
                if (existingRoutingEdge.getProperties().getDistance() > distance) {
                    existingRoutingEdge.getProperties().setDistance(distance);
                    existingRoutingEdge.getProperties().setDirection(direction);
                }
            }
        }

        // Insert new routing neighbor if needed
        if (!foundExisting) {
            EdgeProperties properties = new EdgeProperties();
            properties.setDistance(distance);
            properties.setDirection(direction);
            routingGraph.addEdge(vertex, routingNeighbor, properties);
        }
    }
}
