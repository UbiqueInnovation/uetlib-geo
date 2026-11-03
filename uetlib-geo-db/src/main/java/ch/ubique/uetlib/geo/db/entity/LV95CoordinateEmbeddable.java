package ch.ubique.uetlib.geo.db.entity;

import ch.ubique.uetlib.geo.coordinate.models.LV95Coordinate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

@Embeddable
public class LV95CoordinateEmbeddable {

    @Column(name = "east", nullable = false)
    private double east;

    @Column(name = "north", nullable = false)
    private double north;

    @Column(name = "elevation")
    private @Nullable Double elevation;

    public LV95CoordinateEmbeddable() {}

    public LV95CoordinateEmbeddable(
            final double east, final double north, final @Nullable Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public static LV95CoordinateEmbeddable from(final LV95Coordinate lv95Coordinate) {
        return new LV95CoordinateEmbeddable(
                lv95Coordinate.east(), lv95Coordinate.north(), lv95Coordinate.elevation());
    }

    public LV95Coordinate toCoordinate() {
        return new LV95Coordinate(east, north, elevation);
    }

    public double getEast() {
        return east;
    }

    public void setEast(final double east) {
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(final double north) {
        this.north = north;
    }

    public @Nullable Double getElevation() {
        return elevation;
    }

    public void setElevation(final @Nullable Double elevation) {
        this.elevation = elevation;
    }
}
