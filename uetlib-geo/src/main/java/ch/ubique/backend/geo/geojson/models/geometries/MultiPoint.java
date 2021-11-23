package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiPoint extends Geometry {

    @JsonProperty("coordinates")
    private List<Coordinate> coordinates;

    @JsonIgnore
    public List<Point> getPoints() {
        return coordinates.stream()
                .map(coordinate -> new Point().setCoordinate(coordinate))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public MultiPoint setPoints(List<Point> lineStrings) {
        this.coordinates = lineStrings.stream()
                .map(Point::getCoordinate)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "("
                + getPoints().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
