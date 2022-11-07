package ch.ubique.backend.geo.coordinate.models;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.locationtech.jts.geom.Point;

/**
 * World Geodetic System 1984 (also known as WGS84, WGS 1984, EPSG:4326) Most common coordinate
 * System Also referred to as Lat/Lon-Coordinate
 */
public class WGS84Coordinate implements Coordinate<WGS84Coordinate> {

    // For equality testing
    private static final double EPSILON = 0.001;

    /** y */
    private double latitude;

    /** x */
    private double longitude;

    private Double elevation;

    public WGS84Coordinate(double latitude, double longitude) {
        this(latitude, longitude, null);
    }

    @JsonCreator
    public WGS84Coordinate(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("elevation") Double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public WGS84Coordinate setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public WGS84Coordinate setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Double getElevation() {
        return elevation;
    }

    public WGS84Coordinate setElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.toJtsPoint(this);
    }

    @Override
    public double distanceTo(WGS84Coordinate other) {
        return flatDistanceTo(other);
    }

    @Override
    public double flatDistanceTo(WGS84Coordinate other) {
        var r = 6371; // Radius of the earth in km
        var dLat = toRadians(other.latitude - latitude);
        var dLon = toRadians(other.longitude - longitude);
        var a =
                sin(dLat / 2) * sin(dLat / 2)
                        + cos(toRadians(latitude))
                                * cos(toRadians(other.latitude))
                                * sin(dLon / 2)
                                * sin(dLon / 2);
        var c = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
        var d = r * c; // Distance in [km]
        return d * 1000; // Distance in [m]
    }

    @Override
    public String toString() {
        return "WGS84(lat=" + latitude + ", lon=" + longitude + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WGS84Coordinate that = (WGS84Coordinate) o;

        if (Math.abs(that.latitude - latitude) > EPSILON) return false;
        if (Math.abs(that.longitude - longitude) > EPSILON) return false;
        return Objects.equals(elevation, that.elevation);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
        return result;
    }
}
