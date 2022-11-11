package ch.ubique.backend.geo.coordinate.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Point;

import java.util.Objects;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.LV03;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/** Swiss coordinates (1903) https://epsg.io/21781 Imprecise, use LV95 instead if possible */
public class LV03Coordinate implements Coordinate {

    // For equality testing
    private static final double EPSILON = 0.1; // = 10 cm

    /** x */
    private double east;

    /** y */
    private double north;

    private Double elevation;

    public LV03Coordinate(double east, double north) {
        this(east, north, null);
    }

    @JsonCreator
    public LV03Coordinate(
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

    public LV03Coordinate setEast(double east) {
        this.east = east;
        return this;
    }

    public double getNorth() {
        return north;
    }

    public LV03Coordinate setNorth(double north) {
        this.north = north;
        return this;
    }

    public Double getElevation() {
        return elevation;
    }

    public LV03Coordinate setElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return LV03.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return LV03.toJtsPoint(this);
    }

    @Override
    public double distanceTo(Coordinate other) {
        if (other instanceof LV03Coordinate otherLv03) {
            if (elevation == null || otherLv03.elevation == null) {
                return flatDistanceTo(otherLv03);
            }
            return sqrt(
                    pow(east - otherLv03.east, 2)
                            + pow(north - otherLv03.north, 2)
                            + pow(elevation - otherLv03.elevation, 2));
        }
        throw new IllegalArgumentException("Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public double flatDistanceTo(Coordinate other) {
        if (other instanceof LV03Coordinate otherLv03) {
            return sqrt(pow(east - otherLv03.east, 2) + pow(north - otherLv03.north, 2));
        }
        throw new IllegalArgumentException("Calculating the flat distance between different coordinate systems is not possible.");
    }

    @Override
    public String toString() {
        return "LV03(east=" + east + ", north=" + north + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LV03Coordinate that = (LV03Coordinate) o;

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
