package ch.ubique.backend.geodb.entity;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LV03CoordinateEmbeddable {

    @Column(name = "east", nullable = false)
    private double east;

    @Column(name = "north", nullable = false)
    private double north;

    @Column(name = "elevation")
    private Double elevation;

    public LV03CoordinateEmbeddable() {}

    public LV03CoordinateEmbeddable(final double east, final double north, final Double elevation) {
        this.east = east;
        this.north = north;
        this.elevation = elevation;
    }

    public static LV03CoordinateEmbeddable from(final LV03Coordinate lv03Coordinate) {
        return new LV03CoordinateEmbeddable(
                lv03Coordinate.east(), lv03Coordinate.north(), lv03Coordinate.elevation());
    }

    public LV03Coordinate toCoordinate() {
        return new LV03Coordinate(east, north, elevation);
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
