package ch.ubique.backend.geo.tile.vector.mapbox.tilejson;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record TileJsonVectorLayer(
        @JsonProperty("id") @NotBlank String id,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("minzoom") @Nullable Integer minZoom,
        @JsonProperty("maxzoom") @Nullable Integer maxZoom,
        @JsonProperty("fields") @NotNull Map<String, String> fields) {}
