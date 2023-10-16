package ch.ubique.backend.geo.coordinate.models;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.Point;

import java.util.Optional;

/**
 * World Geodetic System 1984 (also known as WGS84, WGS 1984, EPSG:4326). Most common coordinate
 * System Also referred to as Lat/Lon-Coordinate. <a
 * href="https://epsg.io/4326">https://epsg.io/4326</a>
 */
public final class WGS84Coordinate implements Coordinate {

    @Schema(
            minimum = "-90",
            maximum = "90",
            example = "47.265",
            description = "specifies the north–south position")
    @JsonProperty
    private double latitude;

    @Schema(
            minimum = "-180",
            maximum = "180",
            example = "11.392",
            description = "specifies the east–west position")
    @JsonProperty
    private double longitude;

    @Schema(
            minimum = "-413",
            maximum = "8849",
            example = "574",
            description = "meters above sea level",
            nullable = true)
    @JsonProperty
    private Double elevation;

    public WGS84Coordinate(final double latitude, final double longitude) {
        this(latitude, longitude, null);
    }

    @JsonCreator
    public WGS84Coordinate(
            @JsonProperty("latitude") final double latitude,
            @JsonProperty("longitude") final double longitude,
            @JsonProperty("elevation") final Double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public WGS84Coordinate setLatitude(final double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public WGS84Coordinate setLongitude(final double longitude) {
        this.longitude = longitude;
        return this;
    }

    @Nullable
    public Double getElevation() {
        return elevation;
    }

    public WGS84Coordinate setElevation(final Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @JsonIgnore
    public Optional<Double> getOptionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public int srid() {
        return CoordinateSystems.WGS84.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return CoordinateSystems.WGS84.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.WGS84.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.WGS84.toJtsPoint(this);
    }

    @Override
    public double distanceTo(final Coordinate other) {
        return flatDistanceTo(other);
    }

    @Override
    public double flatDistanceTo(final Coordinate other) {
        if (other instanceof final WGS84Coordinate otherWgs84) {
            final var r = 6371; // Radius of the earth in km
            final var dLat = toRadians(otherWgs84.latitude - latitude);
            final var dLon = toRadians(otherWgs84.longitude - longitude);
            final var a =
                    sin(dLat / 2) * sin(dLat / 2)
                            + cos(toRadians(latitude))
                                    * cos(toRadians(otherWgs84.latitude))
                                    * sin(dLon / 2)
                                    * sin(dLon / 2);
            final var c = 2 * atan2(sqrt(a), sqrt(1 - a));
            final var d = r * c; // Distance in [km]
            return d * 1000; // Distance in [m]
        }

        throw new IllegalArgumentException(
                "Calculating the distance between different coordinate systems is not possible.");
    }

    @Override
    public String toString() {
        return String.format(
                "WGS84(lat=%,.2f, lon=%,.2f, elevation=%,.1f)", latitude, longitude, elevation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WGS84Coordinate that = (WGS84Coordinate) o;
        return new EqualsBuilder()
                .append(latitude, that.latitude)
                .append(longitude, that.longitude)
                .append(elevation, that.elevation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(latitude)
                .append(longitude)
                .append(elevation)
                .toHashCode();
    }
}
