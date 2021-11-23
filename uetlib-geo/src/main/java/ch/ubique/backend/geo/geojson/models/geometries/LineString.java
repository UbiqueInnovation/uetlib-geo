package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineString extends Geometry  {

    private List<double[]> coordinates;

    public LineString setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public List<double[]> getCoordinates() {
        return coordinates;
    }

    @Override
    @JsonIgnore
    public List<double[]> getAllCoordinates() {
        return getCoordinates();
    }
}
