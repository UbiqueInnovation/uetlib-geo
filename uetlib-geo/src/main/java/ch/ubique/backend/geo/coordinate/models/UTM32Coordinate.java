package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.Point;

import java.util.Optional;

public record UTM32Coordinate(
        @Schema(
                        minimum = "-1877994.66",
                        maximum = "3473041.38",
                        example = "465596",
                        description = "specifies the east position")
                @JsonProperty
                double east,
        @Schema(
                        minimum = "3638086.74",
                        maximum = "9494203.2",
                        example = "5228847",
                        description = "specifies the north position")
                @JsonProperty
                double north,
        @Schema(
                        minimum = "-413",
                        maximum = "8849",
                        example = "574",
                        description = "meters above sea level",
                        nullable = true)
                @JsonProperty
                Double elevation)
        implements Coordinate {
    public UTM32Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    @JsonCreator
    public UTM32Coordinate(
            @JsonProperty("east") final double east,
            @JsonProperty("north") final double north,
            @JsonProperty("elevation") final Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public UTM32Coordinate withNorth(final double north) {
        return new UTM32Coordinate(east, north, elevation);
    }

    public UTM32Coordinate withEast(final double east) {
        return new UTM32Coordinate(east, north, elevation);
    }

    public UTM32Coordinate withElevation(final Double elevation) {
        return new UTM32Coordinate(east, north, elevation);
    }

    @JsonIgnore
    public Optional<Double> getOptionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public int srid() {
        return CoordinateSystems.UTM32.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return CoordinateSystems.UTM32.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return CoordinateSystems.UTM32.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.UTM32.toJtsPoint(this);
    }

    @Override
    public String toString() {
        return "UTM32Coordinate{"
                + "east="
                + east
                + ", north="
                + north
                + ", elevation="
                + elevation
                + '}';
    }
}
