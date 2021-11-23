package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Point extends Geometry {

    private double[] coordinates;

    public Point setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    @Override
    @JsonIgnore
    public List<double[]> getAllCoordinates() {
        return List.of(coordinates);
    }
}
