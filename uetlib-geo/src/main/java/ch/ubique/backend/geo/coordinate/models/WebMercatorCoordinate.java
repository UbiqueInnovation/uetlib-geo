package ch.ubique.backend.geo.coordinate.models;

import java.util.Objects;
import org.apache.commons.lang3.NotImplementedException;

/**
 * https://epsg.io/3857 Pseudo-Mercator, Web Mercator
 */
public class WebMercatorCoordinate implements Coordinate<WebMercatorCoordinate> {

    // For equality testing
    private static final double EPSILON = 0.1;

    private double x;
    private double y;
    private Double elevation;

    public WebMercatorCoordinate(double x, double y, Double elevation) {
        this.x = x;
        this.y = y;
        this.elevation = elevation;
    }

    public WebMercatorCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double distanceTo(WebMercatorCoordinate other) {
        throw new NotImplementedException("distancTo not implemented");
    }

    @Override
    public double flatDistanceTo(WebMercatorCoordinate other) {
        throw new NotImplementedException("flatDistancTo not implemented");
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "WebMercator(x=" + x + ", y=" + y + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebMercatorCoordinate that = (WebMercatorCoordinate) o;

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
