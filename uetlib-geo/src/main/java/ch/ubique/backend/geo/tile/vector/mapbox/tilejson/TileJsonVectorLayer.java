package ch.ubique.backend.geo.tile.vector.mapbox.tilejson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

import java.util.Map;

@JsonInclude(Include.NON_NULL)
public record TileJsonVectorLayer(
        @JsonProperty("id") @NotBlank String id,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("minzoom") @Nullable Integer minZoom,
        @JsonProperty("maxzoom") @Nullable Integer maxZoom,
        @JsonProperty("fields") Map<String, String> fields) {}
