package ch.ubique.backend.geo.coordinate.models;

import jakarta.validation.constraints.NotNull;

public record BoundingBox<C extends Coordinate>(@NotNull C min, @NotNull C max) {}
