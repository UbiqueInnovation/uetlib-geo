package ch.ubique.backend.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class VectorLayer {

    @NonNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    /**
     * `minzoom` and `maxzoom` are used to describe the situation where different sets of vector
     * layers appear in different zoom levels of the same tileset, for example in a case where a
     * "minor roads" layer is only present at high zoom levels.
     */
    @JsonProperty("minzoom")
    private Integer minzoom;

    @JsonProperty("maxzoom")
    private Integer maxzoom;

    /** key: field name, value: data type */
    @NotEmpty
    @JsonProperty("fields")
    private Map<String, FieldType> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinzoom() {
        return minzoom;
    }

    public void setMinzoom(Integer minzoom) {
        this.minzoom = minzoom;
    }

    public Integer getMaxzoom() {
        return maxzoom;
    }

    public void setMaxzoom(Integer maxzoom) {
        this.maxzoom = maxzoom;
    }

    public Map<String, FieldType> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldType> fields) {
        this.fields = fields;
    }

    public void addField(String fieldName, FieldType fieldType) {
        if (this.fields == null) {
            this.fields = new HashMap<>();
        }
        this.fields.put(fieldName, fieldType);
    }
}
