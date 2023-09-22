package ch.ubique.backend.geo.coordinate.models;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Optional;

/**
 * Swiss coordinates (1995) <a href="https://epsg.io/2056">https://epsg.io/2056</a> Most recent
 * coordinate system for Switzerland
 */
public final class LV95Coordinate implements Coordinate {

    @Schema(description = "specifies the north–south position", example = "2 600 000")
    @JsonProperty
    private double east;

    @Schema(description = "specifies the east–west position", example = "1 200 000")
    @JsonProperty
    private double north;

    @Schema(description = "meters above sea level", nullable = true)
    @JsonProperty
    private Double elevation;

    public LV95Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    @JsonCreator
    public LV95Coordinate(
            @JsonProperty("east") final double east,
            @JsonProperty("north") final double north,
            @JsonProperty("elevation") final Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public double getEast() {
        return east;
    }

    public LV95Coordinate setEast(final double east) {
        this.east = east;
        return this;
    }

    public double getNorth() {
        return north;
    }

    public LV95Coordinate setNorth(final double north) {
        this.north = north;
        return this;
    }

    @Nullable
    public Double getElevation() {
        return elevation;
    }

    public LV95Coordinate setElevation(final Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @JsonIgnore
    public Optional<Double> getOptionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public int srid() {
        return CoordinateSystems.LV95.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return CoordinateSystems.LV95.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.LV95.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.LV95.toJtsPoint(this);
    }

    @Override
    public double distanceTo(final Coordinate other) {
        if (other instanceof final LV95Coordinate otherLv95) {

            if (elevation == null || otherLv95.elevation == null) {
                return flatDistanceTo(other);
            }
            return sqrt(
                    pow(east - otherLv95.east, 2)
                            + pow(north - otherLv95.north, 2)
                            + pow(elevation - otherLv95.elevation, 2));
        }
        throw new IllegalArgumentException(
                "Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public double flatDistanceTo(final Coordinate other) {
        if (other instanceof final LV95Coordinate otherLv95) {
            return sqrt(pow(east - otherLv95.east, 2) + pow(north - otherLv95.north, 2));
        }
        throw new IllegalArgumentException(
                "Calculating the flat distance between different coordinate systems is not"
                        + " possible.");
    }

    @Override
    public String toString() {
        return String.format(
                "LV95(east=%,.2f, north=%,.2f, elevation=%,.1f)", east, north, elevation);
    }

    /**
     * @deprecated Use {@link #equals(Object)} instead, it compares exact.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public boolean equalsExact(final LV95Coordinate lv95Coordinate) {
        return equals(lv95Coordinate);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LV95Coordinate that = (LV95Coordinate) o;
        return new EqualsBuilder()
                .append(east, that.east)
                .append(north, that.north)
                .append(elevation, that.elevation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(east)
                .append(north)
                .append(elevation)
                .toHashCode();
    }
}
