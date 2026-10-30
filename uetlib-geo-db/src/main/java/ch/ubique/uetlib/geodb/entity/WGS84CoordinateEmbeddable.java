package ch.ubique.uetlib.geodb.entity;

import ch.ubique.uetlib.geo.coordinate.models.WGS84Coordinate;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jspecify.annotations.Nullable;

@Embeddable
public class WGS84CoordinateEmbeddable {

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "elevation")
    private @Nullable Double elevation;

    public WGS84CoordinateEmbeddable() {}

    public WGS84CoordinateEmbeddable(
            final double latitude, final double longitude, final @Nullable Double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public static WGS84CoordinateEmbeddable from(final WGS84Coordinate wgs84Coordinate) {
        return new WGS84CoordinateEmbeddable(
                wgs84Coordinate.latitude(),
                wgs84Coordinate.longitude(),
                wgs84Coordinate.elevation());
    }

    public WGS84Coordinate toCoordinate() {
        return new WGS84Coordinate(latitude, longitude, elevation);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public @Nullable Double getElevation() {
        return elevation;
    }

    public void setElevation(final @Nullable Double elevation) {
        this.elevation = elevation;
    }
}
