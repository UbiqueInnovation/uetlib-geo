package ch.ubique.backend.geo.geometry.models;

public class Point3d extends Point<Point3d> {

    public Point3d(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    protected Point3d create(double... coordinates) {
        return new Point3d(coordinates[0], coordinates[1], coordinates[2]);
    }

    public double getX() {
        return coordinates[0];
    }

    public Point3d setX(double x) {
        this.coordinates[0] = x;
        return this;
    }

    public double getY() {
        return coordinates[1];
    }

    public Point3d setY(double y) {
        this.coordinates[1] = y;
        return this;
    }

    public double getZ() {
        return coordinates[2];
    }

    public Point3d setZ(double z) {
        this.coordinates[2] = z;
        return this;
    }


}
