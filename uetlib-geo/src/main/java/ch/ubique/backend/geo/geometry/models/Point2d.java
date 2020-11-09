package ch.ubique.backend.geo.geometry.models;

public class Point2d extends Point<Point2d> {

    public Point2d(double x, double y) {
        super(x, y);
    }

    @Override
    protected Point2d create(double... coordinates) {
        return new Point2d(coordinates[0], coordinates[1]);
    }

    public double getX() {
        return coordinates[0];
    }

    public Point2d setX(double x) {
        this.coordinates[0] = x;
        return this;
    }

    public double getY() {
        return coordinates[1];
    }

    public Point2d setY(double y) {
        this.coordinates[1] = y;
        return this;
    }
}
