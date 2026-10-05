package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.tile.vector.models.tlm.TlmInfo;

import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Geometry;

public record TlmFeature<G extends Geometry, T extends TlmInfo>(
        @NonNull G geometry, @NonNull T info) {}
