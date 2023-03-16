package ch.ubique.backend.geo.coordinate.models;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WEB_MERCATOR;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Objects;
import java.util.Optional;

/** <a href="https://epsg.io/3857">https://epsg.io/3857</a> Pseudo-Mercator, Web Mercator */
public final class WebMercatorCoordinate implements Coordinate {

    // For equality testing
    private static final double EPSILON = 0.1;

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
        return WEB_MERCATOR.srid();
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return WEB_MERCATOR.crs();
    }

    @Override
    public org.locationtech.jts.geom.Coordinate toJts() {
        return WEB_MERCATOR.toJts(this);
    }

    @Override
    public Point toJtsPoint() {
        return WEB_MERCATOR.toJtsPoint(this);
    }

    @Override
    public double distanceTo(final Coordinate other) {
        throw new NotImplementedException("distanceTo not implemented");
    }

    @Override
    public double flatDistanceTo(final Coordinate other) {
        throw new NotImplementedException("flatDistanceTo not implemented");
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final var that = (WebMercatorCoordinate) o;

        if (Math.abs(that.x - x) > EPSILON) return false;
        if (Math.abs(that.y - y) > EPSILON) return false;
        return Objects.equals(elevation, that.elevation);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
        return result;
    }
}
