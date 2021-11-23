package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    public List<double[]> getAllCoordinates() {
        return geometries.stream()
                .flatMap(geometry -> geometry.getAllCoordinates().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "GeometryCollection("
                + geometries.stream().map(Geometry::toString).collect(Collectors.joining(", "))
                + ")";
    }
}
