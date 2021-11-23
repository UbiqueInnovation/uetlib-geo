package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultiPolygon extends Geometry {

    private List<List<List<double[]>>> coordinates;

    public List<List<List<double[]>>> getCoordinates() {
        return coordinates;
    }

    public MultiPolygon setCoordinates(List<List<List<double[]>>> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    @JsonIgnore
    public List<Polygon> getPolygons() {
        return coordinates.stream()
                .map(coordinate -> new Polygon().setCoordinates(coordinate))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public MultiPolygon setPolygons(List<Polygon> lineStrings) {
        this.coordinates = lineStrings.stream()
                .map(Polygon::getCoordinates)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    @JsonIgnore
    public List<double[]> getAllCoordinates() {
        return coordinates.stream()
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "("
                + getPolygons().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
