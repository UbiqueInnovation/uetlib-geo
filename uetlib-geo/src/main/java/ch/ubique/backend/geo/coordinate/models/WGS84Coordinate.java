package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * World Geodetic System 1984 (also known as WGS84, WGS 1984, EPSG:4326). Most common coordinate
 * System Also referred to as Lat/Lon-Coordinate. <a
 * href="https://epsg.io/4326">https://epsg.io/4326</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WGS84Coordinate(
        @Schema(
                        minimum = "-90",
                        maximum = "90",
                        example = "47.265",
                        description = "specifies the north–south position")
                @JsonProperty(required = true)
                double latitude,
        @Schema(
                        minimum = "-180",
                        maximum = "180",
                        example = "11.392",
                        description = "specifies the east–west position")
                @JsonProperty(required = true)
                double longitude,
        @Schema(
                        minimum = "-413",
                        maximum = "8849",
                        example = "574",
                        description = "meters above sea level",
                        nullable = true)
                @Nullable Double elevation)
        implements Coordinate {

    public WGS84Coordinate(final double latitude, final double longitude) {
        this(latitude, longitude, null);
    }

    public WGS84Coordinate withElevation(final @Nullable Double elevation) {
        return new WGS84Coordinate(latitude, longitude, elevation);
    }

    @JsonIgnore
    public Optional<Double> optionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.WGS84.toJts(this);
    }
}
