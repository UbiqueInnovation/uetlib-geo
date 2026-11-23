package ch.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Vector tileset metadata according to the <a
 * href="https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md#vector-tileset-metadata">MBTiles
 * v1.3 specification</a>.
 *
 * @param vectorLayers vector layer descriptions
 * @param tilestats optional statistics in the <a
 *     href="https://github.com/mapbox/mapbox-geostats#output-the-stats">geostats format</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public record MetadataJson(
        @JsonProperty(value = "vector_layers", required = true) @NotEmpty
                List<VectorLayer> vectorLayers,
        @Nullable Tilestats tilestats) {}
