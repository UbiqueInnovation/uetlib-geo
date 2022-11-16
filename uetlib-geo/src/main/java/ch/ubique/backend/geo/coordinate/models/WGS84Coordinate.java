package ch.ubique.backend.geo.coordinate.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Point;

import java.util.Objects;
import java.util.Optional;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WGS84;
import static java.lang.Math.*;

/**
 * World Geodetic System 1984 (also known as WGS84, WGS 1984, EPSG:4326) Most common coordinate
 * System Also referred to as Lat/Lon-Coordinate
 */
public class WGS84Coordinate implements Coordinate {

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

    public Optional<Double> getElevation() {
        return Optional.ofNullable(elevation);
    }

    public WGS84Coordinate setElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public int srid() {
        return WGS84.srid();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return WGS84.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return WGS84.toJtsPoint(this);
    }

    @Override
    public double distanceTo(Coordinate other) {
        return flatDistanceTo(other);
    }

    @Override
    public double flatDistanceTo(Coordinate other) {
        if (other instanceof WGS84Coordinate otherWgs84) {
            var r = 6371; // Radius of the earth in km
            var dLat = toRadians(otherWgs84.latitude - latitude);
            var dLon = toRadians(otherWgs84.longitude - longitude);
            var a =
                    sin(dLat / 2) * sin(dLat / 2)
                            + cos(toRadians(latitude))
                            * cos(toRadians(otherWgs84.latitude))
                            * sin(dLon / 2)
                            * sin(dLon / 2);
            var c = 2 * atan2(Math.sqrt(a), sqrt(1 - a));
            var d = r * c; // Distance in [km]
            return d * 1000; // Distance in [m]
        }

        throw new IllegalArgumentException("Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public String toString() {
        return String.format("WGS84(lat=%,.2f, lon=%,.2f, elevation=%,.1f)", latitude, longitude, elevation);
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
