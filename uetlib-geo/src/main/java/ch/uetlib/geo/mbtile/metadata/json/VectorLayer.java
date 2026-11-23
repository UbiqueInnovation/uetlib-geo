package ch.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A vector layer's metadata.
 *
 * @param id layer identifier
 * @param description optional layer description
 * @param minzoom optional minimum zoom where the layer is present
 * @param maxzoom optional maximum zoom where the layer is present
 * @param fields mapping from field names to data types
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public record VectorLayer(
        @JsonProperty(required = true) String id,
        @Nullable String description,
        @Nullable Integer minzoom,
        @Nullable Integer maxzoom,
        @JsonProperty(required = true) @NotEmpty Map<String, FieldType> fields) {}
