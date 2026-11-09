package ch.uetlib.geo.graph.algorithms;

import ch.uetlib.geo.graph.models.Vertex;
import ch.uetlib.geo.graph.models.dijkstra.SearchVertex;

import java.util.LinkedList;
import java.util.List;

public final class SearchUtils {
    private SearchUtils() {}

    public static <V, E> List<Vertex<V, E>> computePath(
            final SearchVertex<V, E> targetSearchVertex) {
        final var path = new LinkedList<Vertex<V, E>>();
        var current = targetSearchVertex;
        while (current != null) {
            path.addFirst(current.vertex());
            current = current.previous();
        }
        return path;
    }
}
