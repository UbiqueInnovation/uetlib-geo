package ch.uetlib.geo.graph.algorithms;

import ch.uetlib.geo.graph.models.Graph;
import ch.uetlib.geo.graph.models.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class ConnectedComponents {
    private ConnectedComponents() {}

    public static <V, E> Collection<Collection<Vertex<V, E>>> computeComponents(
            final Graph<V, E> graph) {

        final var visited = new HashSet<Integer>();
        final var connectedComponents = new ArrayList<Collection<Vertex<V, E>>>();

        for (final var vertex : graph.getVertices()) {
            if (visited.contains(vertex.getId())) {
                continue;
            }
            final var currentComponent = new HashSet<Vertex<V, E>>();

            final var queue = new LinkedList<Vertex<V, E>>();
            queue.add(vertex);
            while (!queue.isEmpty()) {
                final var current = queue.pop();
                currentComponent.add(current);

                for (final var neighbor : current.getNeighbors()) {
                    if (!visited.contains(neighbor.getId())) {
                        visited.add(neighbor.getId());
                        queue.add(neighbor);
                    }
                }
            }

            connectedComponents.add(currentComponent);
        }

        return connectedComponents;
    }
}
