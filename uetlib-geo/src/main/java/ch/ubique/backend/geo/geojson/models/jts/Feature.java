package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.n52.jackson.datatype.jts.GeometryDeserializer;

import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    @JsonInclude private String id;

    @JsonInclude private String type;

    @JsonInclude private Map<String, Object> properties;

    @JsonInclude private Geometry geometry;

    @JsonIgnore
    public <T> T getProperty(String key) {
        if (properties.containsKey(key)) {
            return (T) properties.get(key);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public Feature setId(String id) {
        this.id = id;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Feature setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    @JsonDeserialize(contentUsing = GeometryDeserializer.class)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Polygon.class, name = "Polygon"),
        @JsonSubTypes.Type(value = MultiPolygon.class, name = "MultiPolygon"),
        @JsonSubTypes.Type(value = LineString.class, name = "LineString"),
        @JsonSubTypes.Type(value = MultiLineString.class, name = "MultiLineString"),
        @JsonSubTypes.Type(value = Point.class, name = "Point"),
        @JsonSubTypes.Type(value = MultiPoint.class, name = "MultiPoint"),
    })
    public Feature setGeometry(Geometry geometry) {
        this.geometry = geometry;
        return this;
    }

    public String getType() {
        return type;
    }

    public Feature setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "Feature{\n"
                + "  id: \""
                + id
                + "\",\n"
                + "  type: \""
                + type
                + "\",\n"
                + "  properties: {\n    "
                + properties.entrySet().stream()
                        .map(entry -> entry.getKey() + ": \"" + entry.getValue() + "\"")
                        .collect(Collectors.joining("\n    "))
                + "\n},\n"
                + "  geometry: \""
                + geometry.getClass().getSimpleName()
                + "\",\n"
                + "}";
    }
}
