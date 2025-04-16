package ch.ubique.backend.geo.graph.models.dijkstra;

import ch.ubique.backend.geo.graph.models.Vertex;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SearchVertex<V, E>(
        @NotNull Vertex<V, E> vertex,
        @PositiveOrZero double score,
        @Nullable SearchVertex<V, E> previous)
        implements Comparable<SearchVertex<V, E>> {

    @Override
    public int compareTo(@NotNull final SearchVertex<V, E> other) {
        return Double.compare(score, other.score);
    }
}
