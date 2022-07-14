package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Polygon extends Geometry {

    @JsonProperty("coordinates")
    private List<List<Coordinate>> coordinateArray;

    @JsonIgnore
    public List<List<Coordinate>> getCoordinateArray() {
        return coordinateArray;
    }

    @JsonIgnore
    public Polygon setCoordinateArray(List<List<Coordinate>> array) {
        this.coordinateArray = array;
        return this;
    }

    @JsonIgnore
    public List<Coordinate> getExteriorRing() {
        return coordinateArray.get(0);
    }

    @JsonIgnore
    public List<List<Coordinate>> getInteriorRings() {
        return coordinateArray.subList(1, coordinateArray.size());
    }

    @Override
    public List<Coordinate> getAllCoordinates() {
        return coordinateArray.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "("
                + coordinateArray.stream()
                        .map(
                                ring ->
                                        "["
                                                + ring.stream()
                                                        .map(List::toString)
                                                        .collect(Collectors.joining(", "))
                                                + "]")
                        .collect(Collectors.joining(", "))
                + ")";
    }
}
