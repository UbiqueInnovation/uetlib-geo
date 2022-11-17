package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated Please use {@link org.locationtech.jts.geom.MultiPolygon} instead. Will be removed
 *     with 0.9.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
public class MultiPolygon extends Geometry {

    @JsonProperty("coordinates")
    private List<List<List<Coordinate>>> coordinates;

    @JsonIgnore
    public List<Polygon> getPolygons() {
        return coordinates.stream()
                .map(coordinateArray -> new Polygon().setCoordinateArray(coordinateArray))
                .toList();
    }

    @JsonIgnore
    public MultiPolygon setPolygons(List<Polygon> polygons) {
        this.coordinates = polygons.stream().map(Polygon::getCoordinateArray).toList();
        return this;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return coordinates.stream()
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    @JsonIgnore
    public Collection<Geometry> getSimpleGeometries() {
        return getPolygons().stream().map(Geometry.class::cast).toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "("
                + getPolygons().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
