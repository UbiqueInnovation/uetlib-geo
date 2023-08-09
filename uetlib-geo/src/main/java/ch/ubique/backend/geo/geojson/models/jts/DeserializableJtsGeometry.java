package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.locationtech.jts.geom.*;
import org.n52.jackson.datatype.jts.GeometryDeserializer;

@JsonDeserialize(contentUsing = GeometryDeserializer.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Polygon.class, name = "Polygon"),
    @JsonSubTypes.Type(value = MultiPolygon.class, name = "MultiPolygon"),
    @JsonSubTypes.Type(value = LineString.class, name = "LineString"),
    @JsonSubTypes.Type(value = MultiLineString.class, name = "MultiLineString"),
    @JsonSubTypes.Type(value = Point.class, name = "Point"),
    @JsonSubTypes.Type(value = MultiPoint.class, name = "MultiPoint"),
})
public @interface DeserializableJtsGeometry {}
