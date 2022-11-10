package ch.ubique.backend.geo.geojson.models;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Optional;

/**
 * From RFC:
 *
 * <p>A coordinate is an array of numbers. There MUST be two or more elements. The first two
 * elements are longitude and latitude, or easting and northing, precisely in that order and using
 * decimal numbers. Altitude or elevation MAY be included as an optional third element.
 */
public class Coordinate extends ArrayList<Double> {

    @JsonIgnore
    public LV03Coordinate asLv03Coordinate() {
        var east = get(0);
        var north = get(1);
        return new LV03Coordinate(east, north, elevation().orElse(null));
    }

    @JsonIgnore
    public LV95Coordinate asLv95Coordinate() {
        var east = get(0);
        var north = get(1);
        return new LV95Coordinate(east, north, elevation().orElse(null));
    }

    @JsonIgnore
    public WGS84Coordinate asWgs84Coordinate() {
        var lon = get(0);
        var lat = get(1);
        return new WGS84Coordinate(lat, lon, elevation().orElse(null));
    }

    @JsonIgnore
    public WebMercatorCoordinate asWebMercatorCoordinate() {
        var x = get(0);
        var y = get(1);
        return new WebMercatorCoordinate(x, y, elevation().orElse(null));
    }

    @JsonIgnore
    public Optional<Double> elevation() {
        if (size() >= 3) {
            return Optional.of(get(2));
        }
        return Optional.empty();
    }
}
