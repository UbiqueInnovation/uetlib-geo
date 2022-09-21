package ch.ubique.backend.geo.coordinate.models;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.locationtech.jts.geom.Point;

import java.util.Objects;

/** Swiss coordinates (1995) https://epsg.io/2056 Most recent coordinate system for Switzerland */
public class LV95Coordinate implements Coordinate<LV95Coordinate> {

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
    public LV95Coordinate(double east, double north, Double elevation) {
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

    public Double getElevation() {
        return elevation;
    }

    public LV95Coordinate setElevation(Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.toJtsPoint(this);
    }

    @Override
    public double distanceTo(LV95Coordinate other) {
        if (elevation == null || other.elevation == null) {
            return flatDistanceTo(other);
        }
        return sqrt(
                pow(east - other.east, 2)
                        + pow(north - other.north, 2)
                        + pow(elevation - other.elevation, 2));
    }

    @Override
    public double flatDistanceTo(LV95Coordinate other) {
        return sqrt(pow(east - other.east, 2) + pow(north - other.north, 2));
    }

    @Override
    public String toString() {
        return "LV95(east=" + east + ", north=" + north + ')';
    }

    public boolean equalsExact(LV95Coordinate o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LV95Coordinate that = o;

        if (Math.abs(that.east - east) > 0.0) return false;
        if (Math.abs(that.north - north) > 0.0) return false;
        return Objects.equals(elevation, that.elevation);
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
