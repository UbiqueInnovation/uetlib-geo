package ch.ubique.backend.geo.graph.algorithms;

import ch.ubique.backend.geo.graph.models.Edge;
import ch.ubique.backend.geo.graph.models.Vertex;
import ch.ubique.backend.geo.graph.models.dijkstra.SearchVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;

public class Dijkstra<V, E> {

    private final PriorityQueue<SearchVertex<V, E>> queue;
    private final HashSet<Vertex<V, E>> visited;
    private final ToDoubleFunction<Edge<V, E>> edgeScoringFunction;

    public Dijkstra(final ToDoubleFunction<Edge<V, E>> edgeScoringFunction) {
        this.queue = new PriorityQueue<>();
        this.edgeScoringFunction = edgeScoringFunction;
        this.visited = new HashSet<>();
    }

    public Dijkstra(
            final ToDoubleFunction<Edge<V, E>> edgeScoringFunction,
            final Vertex<V, E> startVertex) {
        this(edgeScoringFunction);
        addStartVertex(startVertex);
    }

    public Dijkstra<V, E> addStartVertex(final Vertex<V, E> vertex) {
        queue.add(new SearchVertex<>(vertex, 0, null));
        return this;
    }

    public SearchVertex<V, E> findNext() {
        while (!queue.isEmpty()) {
            final var cur = queue.poll();
            if (visited.contains(cur.vertex())) {
                continue;
            }
            visited.add(cur.vertex());

            for (final var edge : cur.vertex().outgoingEdges()) {
                final var neighbor = edge.getTo();
                if (!visited.contains(neighbor)) {
                    queue.add(
                            new SearchVertex<>(
                                    neighbor,
                                    cur.score() + edgeScoringFunction.applyAsDouble(edge),
                                    cur));
                }
            }
            return cur;
        }
        return null;
    }

    public SearchVertex<V, E> find(final Vertex<V, E> target) {
        SearchVertex<V, E> current;
        while ((current = findNext()) != null) {
            if (current.vertex().equals(target)) {
                return current;
            }
        }
        return null;
    }

    public Collection<SearchVertex<V, E>> find(final Collection<Vertex<V, E>> targets) {
        final var foundTargets = new ArrayList<SearchVertex<V, E>>(targets.size());
        SearchVertex<V, E> current;
        while ((current = findNext()) != null) {
            if (targets.contains(current.vertex())) {
                foundTargets.add(current);
            }
            if (foundTargets.size() == targets.size()) {
                return foundTargets;
            }
        }
        return foundTargets;
    }
}
