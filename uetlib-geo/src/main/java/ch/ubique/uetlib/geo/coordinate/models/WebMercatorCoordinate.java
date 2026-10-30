package ch.ubique.uetlib.geo.coordinate.models;

import ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.uetlib.geo.coordinate.util.JtsConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** <a href="https://epsg.io/3857">https://epsg.io/3857</a> Pseudo-Mercator, Web Mercator */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebMercatorCoordinate(
        @Schema(description = "specifies the north–south position") @JsonProperty(required = true)
                double x,
        @Schema(description = "specifies the east–west position") @JsonProperty(required = true)
                double y,
        @Schema(description = "meters above sea level", nullable = true) @Nullable Double elevation)
        implements Coordinate {

    public WebMercatorCoordinate(final double x, final double y) {
        this(x, y, null);
    }

    public WebMercatorCoordinate withElevation(final @Nullable Double elevation) {
        return new WebMercatorCoordinate(x, y, elevation);
    }

    @Override
    public CoordinateSystem<WebMercatorCoordinate> coordinateSystem() {
        return CoordinateSystems.WEB_MERCATOR;
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.WEB_MERCATOR.toJts(this);
    }

    @JsonIgnore
    public Optional<Double> optionalElevation() {
        return Optional.ofNullable(elevation);
    }
}
