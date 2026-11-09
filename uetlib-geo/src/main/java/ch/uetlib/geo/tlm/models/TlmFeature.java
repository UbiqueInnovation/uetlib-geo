package ch.uetlib.geo.tlm.models;

import ch.uetlib.geo.tile.vector.models.tlm.TlmInfo;

import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Geometry;

public record TlmFeature<G extends Geometry, T extends TlmInfo>(
        @NonNull G geometry, @NonNull T info) {}
