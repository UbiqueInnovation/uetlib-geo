package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.*;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record TlmPathInfo(
        @NonNull UnstructuredTlmInfo unstructured,
        @NonNull String uuid,
        @NonNull WegType type,
        @Nullable WanderwegType trailType,
        @Nullable Integer level,
        @Nullable BelagType surfaceType,
        @Nullable Verkehrsbedeutung significanceForTraffic,
        @NonNull TrafficRestriction trafficRestriction,
        @Nullable Structure structure,
        @Nullable String name,
        @Nullable String streetName,
        boolean directionsSeparated,
        boolean isRoundabout,
        @Nullable Boolean passable)
        implements TlmInfo {

    public TlmPathInfo(final UnstructuredTlmInfo unstructured) {
        this(
                unstructured,
                unstructured.getString("uuid").orElseThrow(),
                unstructured.getString("objektart").map(WegType::parse).orElseThrow(),
                unstructured.getEnum(WanderwegType.class, "wanderwege").orElse(null),
                unstructured.getInt("stufe").orElse(null),
                unstructured.getEnum(BelagType.class, "belagsart").orElse(null),
                unstructured.getEnum(Verkehrsbedeutung.class, "verkehrsbedeutung").orElse(null),
                unstructured
                        .getString("verkehrsbeschraenkung")
                        .map(TrafficRestriction::parse)
                        .orElse(TrafficRestriction.NONE),
                unstructured.getString("kunstbaute").map(Structure::parse).orElse(null),
                unstructured.getString("name").orElse(null),
                unstructured.getString("strassenname").orElse(null),
                unstructured.getBoolean("richtungsgetrennt").orElse(false),
                unstructured.getBoolean("kreisel").orElse(false),
                unstructured.getBoolean("befahrbarkeit").orElse(null));
    }
}
