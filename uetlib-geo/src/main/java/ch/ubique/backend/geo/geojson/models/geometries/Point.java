package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Point extends Geometry {

    @JsonProperty(value = "coordinates")
    private Coordinate coordinate;

    public Point setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return List.of(coordinate);
    }

    @Override
    @JsonIgnore
    public Collection<Geometry> getSimpleGeometries() {
        return List.of(this);
    }

    @JsonIgnore
    public MultiPoint multi() {
        return new MultiPoint().setPoints(List.of(this));
    }
}
