package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.BelagType;
import ch.ubique.backend.geo.tlm.models.Verkehrsbedeutung;
import ch.ubique.backend.geo.tlm.models.WanderwegType;
import ch.ubique.backend.geo.tlm.models.WegType;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record TlmTags(
        @JsonProperty("uuid") @NonNull String uuid,
        @JsonProperty("type") @NonNull WegType type,
        @JsonProperty("wanderwegType") @Nullable WanderwegType trailType,
        @JsonProperty("stufe") @Nullable Integer level,
        @JsonProperty("belagsart") @Nullable BelagType surfaceType,
        @JsonProperty("verkehrsbedeutung") @Nullable Verkehrsbedeutung significanceForTraffic) {
    public TlmTags(final TlmPathInfo tlmPathInfo) {
        this(
                tlmPathInfo.uuid(),
                tlmPathInfo.type(),
                tlmPathInfo.trailType(),
                tlmPathInfo.level(),
                tlmPathInfo.surfaceType(),
                tlmPathInfo.significanceForTraffic());
    }
}
