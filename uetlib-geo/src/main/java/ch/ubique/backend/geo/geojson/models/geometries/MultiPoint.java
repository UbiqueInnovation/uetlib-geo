package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiPoint extends Geometry {
    private List<double[]> coordinates;

    public MultiPoint setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public List<double[]> getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public List<Point> getPoints() {
        return coordinates.stream()
                .map(coordinate -> new Point().setCoordinates(coordinate))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public MultiPoint setPoints(List<Point> lineStrings) {
        this.coordinates = lineStrings.stream()
                .map(Point::getCoordinates)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    @JsonIgnore
    public List<double[]> getAllCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "("
                + getPoints().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
