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
public class MultiLineString extends Geometry {

    @JsonProperty("coordinates")
    private List<List<Coordinate>> coordinates;

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
    public List<Coordinate> getAllCoordinates() {
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
