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
public class Layer {

    @SuppressWarnings("java:S1700")
    @JsonProperty("layer")
    private String layer;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("geometry")
    private String geometry;

    @JsonProperty("attributeCount")
    private Integer attributeCount;

    @JsonProperty("attributes")
    private List<Attribute> attributes;

    public String getLayer() {
        return layer;
    }

    public void setLayer(final String layer) {
        this.layer = layer;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(final String geometry) {
        this.geometry = geometry;
    }

    public Integer getAttributeCount() {
        return attributeCount;
    }

    public void setAttributeCount(final Integer attributeCount) {
        this.attributeCount = attributeCount;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(final Attribute attribute) {
        if (attributes == null) {
            this.attributes = new ArrayList<>();
        }
        attributes.add(attribute);
    }
}
