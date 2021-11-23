package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Polygon extends Geometry {

    private List<List<double[]>> coordinates;


    public List<List<double[]>> getCoordinates() {
        return coordinates;
    }

    public Polygon setCoordinates(List<List<double[]>> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    @JsonIgnore
    public List<double[]> getExteriorRing() {
        return coordinates.get(0);
    }

    @JsonIgnore
    public List<List<double[]>> getInteriorRings() {
        return coordinates.subList(1, coordinates.size());
    }

    @Override
    public List<double[]> getAllCoordinates() {
        return coordinates.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "("
                + coordinates.stream()
                .map(ring -> "[" + ring.stream().map(Arrays::toString).collect(Collectors.joining(", ")) + "]")
                .collect(Collectors.joining(", "))
                + ")";
    }
}
