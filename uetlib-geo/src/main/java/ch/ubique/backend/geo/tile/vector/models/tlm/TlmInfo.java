package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TlmInfo(
        @JsonProperty("uuid")
        String uuid,
        @JsonProperty("type")
        WegType type,
        @JsonProperty("wanderwegType")
        WanderwegType trailType,
        @JsonProperty("stufe")
        Integer level,
        @JsonProperty("belagsart")
        BelagType surfaceType,
        @JsonProperty("verkehrsbedeutung")
        Verkehrsbedeutung significanceForTraffic
) {
    public TlmInfo(final TlmPath tlmPath) {
        this(
                tlmPath.getUuid(),
                tlmPath.getType(),
                tlmPath.getWanderwegType(),
                tlmPath.getStufe(),
                tlmPath.getBelagsart(),
                tlmPath.getVerkehrsbedeutung()
        );
    }
}
