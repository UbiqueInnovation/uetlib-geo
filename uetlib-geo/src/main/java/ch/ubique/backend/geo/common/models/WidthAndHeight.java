package ch.ubique.backend.geo.common.models;

import org.jspecify.annotations.NonNull;

public record WidthAndHeight<T>(@NonNull T width, @NonNull T height) {
    public static <T> WidthAndHeight<T> of(@NonNull final T width, @NonNull final T height) {
        return new WidthAndHeight<>(width, height);
    }
}
