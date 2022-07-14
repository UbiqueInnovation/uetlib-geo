package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultiPolygon extends Geometry {

    @JsonProperty("coordinates")
    private List<List<List<Coordinate>>> coordinates;

    @JsonIgnore
    public List<Polygon> getPolygons() {
        return coordinates.stream()
                .map(coordinateArray -> new Polygon().setCoordinateArray(coordinateArray))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public MultiPolygon setPolygons(List<Polygon> polygons) {
        this.coordinates =
                polygons.stream().map(Polygon::getCoordinateArray).collect(Collectors.toList());
        return this;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return coordinates.stream()
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "("
                + getPolygons().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
