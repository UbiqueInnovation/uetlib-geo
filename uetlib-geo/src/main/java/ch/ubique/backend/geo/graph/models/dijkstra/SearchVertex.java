package ch.ubique.backend.geo.graph.models.dijkstra;

import ch.ubique.backend.geo.graph.models.Vertex;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SearchVertex<V, E>(
        @NonNull Vertex<V, E> vertex,
        @PositiveOrZero double score,
        @Nullable SearchVertex<V, E> previous)
        implements Comparable<SearchVertex<V, E>> {

    @Override
    public int compareTo(@NonNull final SearchVertex<V, E> other) {
        return Double.compare(score, other.score);
    }
}
