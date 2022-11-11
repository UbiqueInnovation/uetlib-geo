package ch.ubique.backend.geo.geojson.models;

import ch.ubique.backend.geo.geojson.models.geometries.GeometryCollection;
import ch.ubique.backend.geo.geojson.models.geometries.LineString;
import ch.ubique.backend.geo.geojson.models.geometries.MultiLineString;
import ch.ubique.backend.geo.geojson.models.geometries.MultiPoint;
import ch.ubique.backend.geo.geojson.models.geometries.MultiPolygon;
import ch.ubique.backend.geo.geojson.models.geometries.Point;
import ch.ubique.backend.geo.geojson.models.geometries.Polygon;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated Please use {@link org.locationtech.jts.geom.Geometry} instead. Will be removed with
 *     0.8.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(GeometryCollection.class),
    @JsonSubTypes.Type(LineString.class),
    @JsonSubTypes.Type(MultiLineString.class),
    @JsonSubTypes.Type(MultiPoint.class),
    @JsonSubTypes.Type(MultiPolygon.class),
    @JsonSubTypes.Type(Point.class),
    @JsonSubTypes.Type(Polygon.class)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Geometry {

    @JsonIgnore
    public abstract List<Coordinate> getAllCoordinates();

    /**
     * Returns a list of simple geometries. I.e. MultiGeometries are converted to a list of
     * Geometries. E.g. A MultiPolygon is converted to a list of Polygons.
     */
    @JsonIgnore
    public abstract Collection<Geometry> getSimpleGeometries();

    @Override
    public String toString() {
        String coordinates =
                getAllCoordinates().stream().map(List::toString).collect(Collectors.joining(", "));
        return String.format("%s(%s)", getClass().getSimpleName(), coordinates);
    }
}
