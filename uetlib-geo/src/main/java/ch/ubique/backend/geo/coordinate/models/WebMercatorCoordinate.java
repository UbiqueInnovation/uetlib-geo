package ch.ubique.backend.geo.coordinate.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.NotImplementedException;
import org.locationtech.jts.geom.Point;

import java.util.Objects;
import java.util.Optional;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WEB_MERCATOR;

/** https://epsg.io/3857 Pseudo-Mercator, Web Mercator */
public class WebMercatorCoordinate implements Coordinate {

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
            @JsonProperty("x") double x,
            @JsonProperty("y") double y,
            @JsonProperty("elevation") Double elevation) {
        this.x = x;
        this.y = y;
        this.elevation = elevation;
    }

    public WebMercatorCoordinate(double x, double y) {
        this(x, y, null);
    }

    @Override
    public int srid() {
        return WEB_MERCATOR.srid();
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
    public double distanceTo(Coordinate other) {
        throw new NotImplementedException("distanceTo not implemented");
    }

    @Override
    public double flatDistanceTo(Coordinate other) {
        throw new NotImplementedException("flatDistanceTo not implemented");
    }

    public double getX() {
        return x;
    }

    public WebMercatorCoordinate setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public WebMercatorCoordinate setY(double y) {
        this.y = y;
        return this;
    }

    @JsonIgnore
    public Optional<Double> getElevation() {
        return Optional.ofNullable(elevation);
    }

    public WebMercatorCoordinate setElevation(final Double elevation) {
        this.elevation = elevation;
        return this;
    }

    @Override
    public String toString() {
        return String.format("WebMercator(x=%,.2f, y=%,.2f, elevation=%,.1f)", x, y, elevation);
    }

    @Override
    public boolean equals(Object o) {
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
