package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.*;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TlmPathInfo(
        @NotNull String uuid,
        @NotNull WegType type,
        @Nullable WanderwegType trailType,
        @Nullable Integer level,
        @Nullable BelagType surfaceType,
        @Nullable Verkehrsbedeutung significanceForTraffic,
        @NotNull TrafficRestriction trafficRestriction,
        @Nullable String name,
        @Nullable String streetName,
        boolean directionsSeparated,
        boolean isRoundabout,
        @Nullable Boolean passable) {}
