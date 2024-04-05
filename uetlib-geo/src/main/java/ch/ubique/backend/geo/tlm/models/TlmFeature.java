package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.tile.vector.models.tlm.TlmInfo;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.Geometry;

public record TlmFeature<G extends Geometry, T extends TlmInfo>(
        @NotNull G geometry, @NotNull T info) {}
