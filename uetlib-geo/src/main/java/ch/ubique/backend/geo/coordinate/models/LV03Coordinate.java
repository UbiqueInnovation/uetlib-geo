package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Swiss coordinates (1903) <a href="https://epsg.io/21781">https://epsg.io/21781</a> Imprecise, use
 * LV95 instead if possible
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LV03Coordinate(
        @Schema(description = "specifies the north–south position", example = "600 000")
                @JsonProperty(required = true)
                double east,
        @Schema(description = "specifies the east–west position", example = "200 000")
                @JsonProperty(required = true)
                double north,
        @Schema(description = "meters above sea level", nullable = true) @Nullable Double elevation)
        implements Coordinate {

    public LV03Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    public LV03Coordinate withElevation(final @Nullable Double elevation) {
        return new LV03Coordinate(east, north, elevation);
    }

    @JsonIgnore
    public Optional<Double> optionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public CoordinateSystem<LV03Coordinate> coordinateSystem() {
        return CoordinateSystems.LV03;
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.LV03.toJts(this);
    }
}
