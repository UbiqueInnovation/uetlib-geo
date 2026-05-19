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
 * Swiss coordinates (1995) <a href="https://epsg.io/2056">https://epsg.io/2056</a> Most recent
 * coordinate system for Switzerland
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LV95Coordinate(
        @Schema(description = "specifies the north–south position", example = "2 600 000")
                @JsonProperty(required = true)
                double east,
        @Schema(description = "specifies the east–west position", example = "1 200 000")
                @JsonProperty(required = true)
                double north,
        @Schema(description = "meters above sea level", nullable = true) @Nullable Double elevation)
        implements Coordinate {

    public LV95Coordinate(final double east, final double north) {
        this(east, north, null);
    }

    public LV95Coordinate withElevation(@Nullable final Double elevation) {
        return new LV95Coordinate(east, north, elevation);
    }

    @JsonIgnore
    public Optional<Double> optionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public CoordinateSystem<LV95Coordinate> coordinateSystem() {
        return CoordinateSystems.LV95;
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.LV95.toJts(this);
    }
}
