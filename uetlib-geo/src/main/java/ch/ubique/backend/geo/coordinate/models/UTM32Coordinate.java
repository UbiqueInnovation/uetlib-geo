package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
                @Nullable Double elevation)
        implements Coordinate {
    public UTM32Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    public UTM32Coordinate withElevation(final @Nullable Double elevation) {
        return new UTM32Coordinate(east, north, elevation);
    }

    @JsonIgnore
    public Optional<Double> optionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return CoordinateSystems.UTM32.toJts(this);
    }
}
