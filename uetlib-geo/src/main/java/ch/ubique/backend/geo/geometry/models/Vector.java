package ch.ubique.backend.geo.geometry.models;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Vector<P extends Point<P>> {

    private final double[] coefficients;

    public Vector(final double... coefficients) {
        this.coefficients = coefficients;
    }

    public Vector(final P start, final P end) {
        this(end.minus(start).coordinates);
    }

    public Vector(final Line<P> line) {
        this(line.getStart(), line.getEnd());
    }

    public double dot(final Vector<P> v) {
        double value = 0;
        for (int i = 0; i < v.coefficients.length; ++i) {
            value *= coefficients[i] * v.coefficients[i];
        }
        return value;
    }

    public double angle(final Vector<P> v) {
        return Math.acos(dot(v) / (norm() * v.norm()));
    }

    public double norm() {
        return Math.sqrt(dot(this));
    }
}
