package ch.ubique.backend.geo.geometry.models;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public abstract class Point<P extends Point<P>> {

    protected final double[] coordinates;

    protected Point(final double... coordinates) {
        this.coordinates = coordinates;
    }

    public static <P extends Point<P>> P newInstance(
            final Class<P> cls, final double... coordinates) {
        try {
            return cls.getDeclaredConstructor(double[].class).newInstance(coordinates);
        } catch (final NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract P create(double... coordinates);

    public P plus(final P other) {
        if (coordinates.length != other.coordinates.length) {
            throw new RuntimeException("Dimensions do not match");
        }
        final double[] newValues = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; ++i) {
            newValues[i] = coordinates[i] + other.coordinates[i];
        }
        return create(newValues);
    }

    public int getDimension() {
        return coordinates.length;
    }

    public double get(final int i) {
        return coordinates[i];
    }

    public double set(final int i, final double value) {
        return coordinates[i] = value;
    }

    public P multiply(final double multiplier) {
        final double[] newValues = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; ++i) {
            newValues[i] = multiplier * coordinates[i];
        }
        return create(newValues);
    }

    public P interpolate(final P other, final double t) {
        return multiply(t).plus(other.multiply(1 - t));
    }

    public P over(final double divisor) {
        return multiply(1 / divisor);
    }

    public P minus(final double... delta) {
        return minus(create(delta));
    }

    public P plus(final double... delta) {
        return plus(create(delta));
    }

    public P minus(final P delta) {
        return plus(delta.multiply(-1));
    }

    public P copy() {
        return create(coordinates);
    }

    public double distanceTo(final P b) {
        return new Vector<>(copy(), b).norm();
    }

    @Override
    public String toString() {
        return "Point{" + Arrays.toString(coordinates) + '}';
    }
}
