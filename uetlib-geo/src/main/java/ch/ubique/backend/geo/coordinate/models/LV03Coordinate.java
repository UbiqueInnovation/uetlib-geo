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
 * Swiss coordinates (1903) <a href="https://epsg.io/21781">https://epsg.io/21781</a> Imprecise, use
 * LV95 instead if possible
 */
public final class LV03Coordinate implements Coordinate {

    @Schema(description = "specifies the north–south position", example = "600 000")
    @JsonProperty
    private double east;

    @Schema(description = "specifies the east–west position", example = "200 000")
    @JsonProperty
    private double north;

    @Schema(description = "meters above sea level", nullable = true)
    @JsonProperty
    private Double elevation;

    public LV03Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    @JsonCreator
    public LV03Coordinate(
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

    public LV03Coordinate setEast(final double east) {
        this.east = east;
        return this;
    }

    public double getNorth() {
        return north;
    }

    public LV03Coordinate setNorth(final double north) {
        this.north = north;
        return this;
    }

    @Nullable
    public Double getElevation() {
        return elevation;
    }

    public LV03Coordinate setElevation(final Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @JsonIgnore
    public Optional<Double> getOptionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public int srid() {
        return CoordinateSystems.LV03.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return CoordinateSystems.LV03.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.LV03.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.LV03.toJtsPoint(this);
    }

    @Override
    public double distanceTo(final Coordinate other) {
        if (other instanceof final LV03Coordinate otherLv03) {
            if (elevation == null || otherLv03.elevation == null) {
                return flatDistanceTo(otherLv03);
            }
            return sqrt(
                    pow(east - otherLv03.east, 2)
                            + pow(north - otherLv03.north, 2)
                            + pow(elevation - otherLv03.elevation, 2));
        }
        throw new IllegalArgumentException(
                "Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public double flatDistanceTo(final Coordinate other) {
        if (other instanceof final LV03Coordinate otherLv03) {
            return sqrt(pow(east - otherLv03.east, 2) + pow(north - otherLv03.north, 2));
        }
        throw new IllegalArgumentException(
                "Calculating the flat distance between different coordinate systems is not"
                        + " possible.");
    }

    @Override
    public String toString() {
        return String.format(
                "LV03(east=%,.2f, north=%,.2f, elevation=%,.1f)", east, north, elevation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LV03Coordinate that = (LV03Coordinate) o;
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
