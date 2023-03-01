package ch.ubique.backend.geo.geometry.models;

@Deprecated(since = "1.3.0", forRemoval = true)
public class Point2d extends Point<Point2d> {

    protected Point2d(double... coordinates) {
        this(coordinates[0], coordinates[1]);
    }

    public Point2d(double x, double y) {
        super(x, y);
    }

    @Override
    public Point2d create(double... coordinates) {
        if (coordinates.length < 2) {
            throw new RuntimeException("Not enough coordinates to construct a 2d point");
        }
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

    @Override
    public String toString() {
        return String.format("Point2d{%,f, %,f}", getX(), getY());
    }
}
