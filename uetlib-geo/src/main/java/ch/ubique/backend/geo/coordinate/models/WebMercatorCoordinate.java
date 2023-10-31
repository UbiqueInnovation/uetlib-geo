package ch.ubique.backend.geo.coordinate.models;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.Point;

import java.util.Optional;

/** <a href="https://epsg.io/3857">https://epsg.io/3857</a> Pseudo-Mercator, Web Mercator */
public final class WebMercatorCoordinate implements Coordinate {

    @Schema(description = "specifies the north–south position")
    @JsonProperty
    private double x;

    @Schema(description = "specifies the east–west position")
    @JsonProperty
    private double y;

    @Schema(description = "meters above sea level", nullable = true)
    @JsonProperty
    private Double elevation;

    @JsonCreator
    public WebMercatorCoordinate(
            @JsonProperty("x") final double x,
            @JsonProperty("y") final double y,
            @JsonProperty("elevation") final Double elevation) {
        this.x = x;
        this.y = y;
        this.elevation = elevation;
    }

    public WebMercatorCoordinate(final double x, final double y) {
        this(x, y, null);
    }

    @Override
    public int srid() {
        return CoordinateSystems.WEB_MERCATOR.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return CoordinateSystems.WEB_MERCATOR.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return JtsConverter.WEB_MERCATOR.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return JtsConverter.WEB_MERCATOR.toJtsPoint(this);
    }

    public double getX() {
        return x;
    }

    public WebMercatorCoordinate setX(final double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public WebMercatorCoordinate setY(final double y) {
        this.y = y;
        return this;
    }

    @Nullable
    public Double getElevation() {
        return elevation;
    }

    public WebMercatorCoordinate setElevation(final Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @JsonIgnore
    public Optional<Double> getOptionalElevation() {
        return Optional.ofNullable(elevation);
    }

    @Override
    public String toString() {
        return String.format("WebMercator(x=%,.2f, y=%,.2f, elevation=%,.1f)", x, y, elevation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WebMercatorCoordinate that = (WebMercatorCoordinate) o;
        return new EqualsBuilder()
                .append(x, that.x)
                .append(y, that.y)
                .append(elevation, that.elevation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(x).append(y).append(elevation).toHashCode();
    }
}
