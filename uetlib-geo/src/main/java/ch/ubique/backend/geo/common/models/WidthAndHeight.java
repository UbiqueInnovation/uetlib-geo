package ch.ubique.backend.geo.common.models;

import com.drew.lang.annotations.NotNull;

public record WidthAndHeight<T>(@NotNull T width, @NotNull T height) {}
