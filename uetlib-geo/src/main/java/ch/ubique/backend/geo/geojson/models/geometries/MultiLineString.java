package ch.ubique.backend.geo.geojson.models.geometries;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated Please use {@link org.locationtech.jts.geom.MultiLineString} instead. Will be removed
 *     with 0.8.0
 */
@Deprecated(since = "0.7.14", forRemoval = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiLineString extends Geometry {

    @JsonProperty("coordinates")
    private List<List<Coordinate>> coordinates;

    @JsonIgnore
    public List<LineString> getLineStrings() {
        return coordinates.stream().map(list -> new LineString().setCoordinates(list)).toList();
    }

    @JsonIgnore
    public MultiLineString setLineStrings(List<LineString> lineStrings) {
        this.coordinates = lineStrings.stream().map(LineString::getCoordinates).toList();
        return this;
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getAllCoordinates() {
        return coordinates.stream().flatMap(Collection::stream).toList();
    }

    @Override
    @JsonIgnore
    public Collection<Geometry> getSimpleGeometries() {
        return getLineStrings().stream().map(Geometry.class::cast).toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "("
                + getLineStrings().stream()
                        .map(Geometry::toString)
                        .collect(Collectors.joining(", "))
                + ")";
    }
}
