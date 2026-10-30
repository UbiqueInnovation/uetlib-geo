package ch.ubique.uetlib.geo.mbtile.metadata.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullUnmarked;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@NullUnmarked
public class Attribute {

    @SuppressWarnings("java:S1700")
    @JsonProperty("attribute")
    private String attribute;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("type")
    private String type;

    @JsonProperty("values")
    private List<Object> values;

    @JsonProperty("min")
    private Long min;

    @JsonProperty("max")
    private Long max;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(final List<Object> values) {
        this.values = values;
    }

    public void addValue(final Object value) {
        if (values == null) {
            this.values = new ArrayList<>();
        }
        values.add(value);
    }

    public Long getMin() {
        return min;
    }

    public void setMin(final Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(final Long max) {
        this.max = max;
    }
}
