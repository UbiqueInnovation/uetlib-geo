package ch.ubique.backend.geo.geometry.models;

public class Vector<P extends Point<P>> {

    private final double[] coefficients;

    public Vector(double... coefficients) {
        this.coefficients = coefficients;
    }

    public Vector(P start, P end) {
        this(end.minus(start).coordinates);
    }

    public Vector(Line<P> line) {
        this(line.getStart(), line.getEnd());
    }

    public double dot(Vector<P> v) {
        double value = 0;
        for (int i = 0; i < v.coefficients.length; ++i) {
            value *= coefficients[i] * v.coefficients[i];
        }
        return value;
    }

    public double angle(Vector<P> v) {
        return Math.acos(dot(v) / (norm() * v.norm()));
    }

    public double norm() {
        return Math.sqrt(dot(this));
    }
}
