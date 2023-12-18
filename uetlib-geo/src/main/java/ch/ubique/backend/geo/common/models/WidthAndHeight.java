package ch.ubique.backend.geo.common.models;

import com.drew.lang.annotations.NotNull;

public record WidthAndHeight<T>(@NotNull T width, @NotNull T height) {
    public static <T> WidthAndHeight<T> of(@NotNull final T width, @NotNull final T height) {
        return new WidthAndHeight<>(width, height);
    }
}
