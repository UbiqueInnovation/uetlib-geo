package ch.ubique.backend.geo.tile.vector.mapbox.tilejson;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.jackson.TileJsonBoundsDeserializer;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.jackson.TileJsonBoundsSerializer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record TileJson(
        @JsonProperty("tilejson") @NotBlank String tileJsonVersion,
        @JsonProperty("tiles") @NotEmpty List<String> tiles,
        @JsonProperty("vector_layers") @NotNull List<TileJsonVectorLayer> vectorLayers,
        @JsonProperty("attribution") @Nullable String attribution,
        @JsonProperty("bounds")
                @JsonDeserialize(using = TileJsonBoundsDeserializer.class)
                @JsonSerialize(using = TileJsonBoundsSerializer.class)
                @Nullable
                BoundingBox<WGS84Coordinate> bounds,
        @JsonProperty("center") @Nullable TileJsonCenter center,
        @JsonProperty("data") @Nullable List<String> data,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("fillzoom") @Nullable Integer fillzoom,
        @JsonProperty("grids") @Nullable List<String> grids,
        @JsonProperty("legend") @Nullable String legend,
        @JsonProperty("maxzoom") @Nullable Integer maxZoom,
        @JsonProperty("minzoom") @Nullable Integer minZoom,
        @JsonProperty("name") @Nullable String name,
        @JsonProperty("scheme") @Nullable String scheme,
        @JsonProperty("template") @Nullable String template,
        @JsonProperty("version") @Nullable String version,
        @JsonAnySetter @JsonAnyGetter Map<String, Object> additionalData) {}
