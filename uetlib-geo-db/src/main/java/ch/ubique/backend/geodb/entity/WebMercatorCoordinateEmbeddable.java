package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

@Embeddable
public class WebMercatorCoordinateEmbeddable {

    @Column(name = "x", nullable = false)
    private double x;

    @Column(name = "y", nullable = false)
    private double y;

    @Column(name = "elevation")
    private @Nullable Double elevation;

    public WebMercatorCoordinateEmbeddable() {}

    public WebMercatorCoordinateEmbeddable(
            final double x, final double y, final @Nullable Double elevation) {
        this.x = x;
        this.y = y;
        this.elevation = elevation;
    }

    public static WebMercatorCoordinateEmbeddable from(
            final WebMercatorCoordinate webMercatorCoordinate) {
        return new WebMercatorCoordinateEmbeddable(
                webMercatorCoordinate.x(),
                webMercatorCoordinate.y(),
                webMercatorCoordinate.elevation());
    }

    public WebMercatorCoordinate toCoordinate() {
        return new WebMercatorCoordinate(x, y, elevation);
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public @Nullable Double getElevation() {
        return elevation;
    }

    public void setElevation(final @Nullable Double elevation) {
        this.elevation = elevation;
    }
}
