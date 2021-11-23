package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiLineString extends Geometry {

    private List<List<double[]>> coordinates;

    public MultiLineString setCoordinates(List<List<double[]>> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public List<List<double[]>> getCoordinates() {
        return coordinates;
    }

    @JsonIgnore
    public List<LineString> getLineStrings() {
        return coordinates.stream()
                .map(list -> new LineString().setCoordinates(list))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public MultiLineString setLineStrings(List<LineString> lineStrings) {
        this.coordinates = lineStrings.stream()
                .map(LineString::getCoordinates)
                .collect(Collectors.toList());
        return this;
    }

    @Override
    @JsonIgnore
    public List<double[]> getAllCoordinates() {
        return coordinates.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "("
                + getLineStrings().stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
