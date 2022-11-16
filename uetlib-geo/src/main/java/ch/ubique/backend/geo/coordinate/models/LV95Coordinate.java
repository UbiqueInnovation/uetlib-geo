package ch.ubique.backend.geo.coordinate.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Point;

import java.util.Objects;
import java.util.Optional;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.LV95;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/** Swiss coordinates (1995) https://epsg.io/2056 Most recent coordinate system for Switzerland */
public class LV95Coordinate implements Coordinate {

    // For equality testing
    private static final double EPSILON = 0.1; // = 10 cm

    /** x */
    private double east;

    /** y */
    private double north;

    private Double elevation;

    public LV95Coordinate(double east, double north) {
        this(east, north, null);
    }

    @JsonCreator
    public LV95Coordinate(
            @JsonProperty("east") double east,
            @JsonProperty("north") double north,
            @JsonProperty("elevation") Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public double getEast() {
        return east;
    }

    public LV95Coordinate setEast(double east) {
        this.east = east;
        return this;
    }

    public double getNorth() {
        return north;
    }

    public LV95Coordinate setNorth(double north) {
        this.north = north;
        return this;
    }

    public Optional<Double> getElevation() {
        return Optional.ofNullable(elevation);
    }

    public LV95Coordinate setElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public int srid() {
        return LV95.srid();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return LV95.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return LV95.toJtsPoint(this);
    }

    @Override
    public double distanceTo(Coordinate other) {
        if (other instanceof LV95Coordinate otherLv95) {

            if (elevation == null || otherLv95.elevation == null) {
                return flatDistanceTo(other);
            }
            return sqrt(
                    pow(east - otherLv95.east, 2)
                            + pow(north - otherLv95.north, 2)
                            + pow(elevation - otherLv95.elevation, 2));
        }
        throw new IllegalArgumentException("Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public double flatDistanceTo(Coordinate other) {
        if (other instanceof LV95Coordinate otherLv95) {
            return sqrt(pow(east - otherLv95.east, 2) + pow(north - otherLv95.north, 2));
        }
        throw new IllegalArgumentException("Calculating the flat distance between different coordinate systems is not possible.");
    }

    @Override
    public String toString() {
        return String.format("LV95(east=%,.2f, north=%,.2f, elevation=%,.1f)", east, north, elevation);
    }

    public boolean equalsExact(LV95Coordinate o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (Math.abs(o.east - east) > 0.0) return false;
        if (Math.abs(o.north - north) > 0.0) return false;
        return Objects.equals(elevation, o.elevation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LV95Coordinate that = (LV95Coordinate) o;

        if (Math.abs(that.east - east) > EPSILON) return false;
        if (Math.abs(that.north - north) > EPSILON) return false;
        return Objects.equals(elevation, that.elevation);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(east);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(north);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
        return result;
    }
}
