package ch.ubique.backend.geo.graph.models.dijkstra;

import ch.ubique.backend.geo.graph.models.Vertex;

import jakarta.validation.constraints.PositiveOrZero;

import org.jspecify.annotations.Nullable;

public record SearchVertex<V, E>(
        Vertex<V, E> vertex, @PositiveOrZero double score, @Nullable SearchVertex<V, E> previous)
        implements Comparable<SearchVertex<V, E>> {

    @Override
    public int compareTo(final SearchVertex<V, E> other) {
        return Double.compare(score, other.score);
    }
}
