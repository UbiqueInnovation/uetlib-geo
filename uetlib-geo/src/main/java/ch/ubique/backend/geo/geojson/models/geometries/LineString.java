package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineString extends Geometry {

    @JsonProperty("coordinates")
    private List<Coordinate> coordinates;

    public LineString setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return getCoordinates();
    }
}
