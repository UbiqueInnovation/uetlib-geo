package ch.ubique.backend.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * according to mbtiles v1.3 specifications
 * (https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md#vector-tileset-metadata)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MetadataJson {

    @JsonProperty("vector_layers")
    @NotNull
    @NotEmpty
    private List<VectorLayer> vectorLayers;

    /** in the geostats format (https://github.com/mapbox/mapbox-geostats#output-the-stats) */
    @JsonProperty("tilestats")
    private Tilestats tilestats;

    public List<VectorLayer> getVectorLayers() {
        return vectorLayers;
    }

    public void setVectorLayers(List<VectorLayer> vectorLayers) {
        this.vectorLayers = vectorLayers;
    }

    public void addVectorLayer(VectorLayer vectorLayer) {
        if (this.vectorLayers == null) {
            this.vectorLayers = new ArrayList<>();
        }
        this.vectorLayers.add(vectorLayer);
    }

    public Tilestats getTilestats() {
        return tilestats;
    }

    public void setTilestats(Tilestats tilestats) {
        this.tilestats = tilestats;
    }
}
