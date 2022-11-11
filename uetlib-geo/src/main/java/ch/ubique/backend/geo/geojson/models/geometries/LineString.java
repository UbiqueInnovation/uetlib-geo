package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;

/**
 * @deprecated Please use {@link org.locationtech.jts.geom.LineString} instead. Will be removed with
 *     0.8.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
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

    @Override
    @JsonIgnore
    public Collection<Geometry> getSimpleGeometries() {
        return List.of(this);
    }

    @JsonIgnore
    public MultiLineString multi() {
        return new MultiLineString().setLineStrings(List.of(this));
    }
}
