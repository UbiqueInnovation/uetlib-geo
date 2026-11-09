package ch.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jspecify.annotations.NullUnmarked;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@NullUnmarked
public class Tilestats {

    @JsonProperty("layerCount")
    private Integer layerCount;

    @JsonProperty("layers")
    private List<Layer> layers;

    public Integer getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(final Integer layerCount) {
        this.layerCount = layerCount;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(final List<Layer> layers) {
        this.layers = layers;
    }

    public void addLayer(final Layer layer) {
        if (layers == null) {
            this.layers = new ArrayList<>();
        }
        layers.add(layer);
    }
}
