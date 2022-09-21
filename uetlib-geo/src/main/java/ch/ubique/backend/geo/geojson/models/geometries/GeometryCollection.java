package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeometryCollection extends Geometry {

    private List<Geometry> geometries;

    public List<Geometry> getGeometries() {
        return geometries;
    }

    public GeometryCollection setGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
        return this;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return geometries.stream()
                .flatMap(geometry -> geometry.getAllCoordinates().stream())
                .toList();
    }

    @Override
    public Collection<Geometry> getSimpleGeometries() {
        return geometries.stream()
                .flatMap(geometry -> geometry.getSimpleGeometries().stream())
                .toList();
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "GeometryCollection("
                + geometries.stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
