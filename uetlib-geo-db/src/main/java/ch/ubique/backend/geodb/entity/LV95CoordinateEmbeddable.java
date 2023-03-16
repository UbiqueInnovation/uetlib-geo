package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LV95CoordinateEmbeddable {

    @Column(name = "east", nullable = false)
    private double east;

    @Column(name = "north", nullable = false)
    private double north;

    @Column(name = "elevation")
    private Double elevation;

    public LV95CoordinateEmbeddable() {}

    public LV95CoordinateEmbeddable(final double east, final double north, final Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public static LV95CoordinateEmbeddable from(final LV95Coordinate lv95Coordinate) {
        return new LV95CoordinateEmbeddable(
                lv95Coordinate.getEast(), lv95Coordinate.getNorth(), lv95Coordinate.getElevation());
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

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(final Double elevation) {
        this.elevation = elevation;
    }
}
