package ch.ubique.backend.geo.geometry.models;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class Point <P extends Point<P>> {

    protected final double[] coordinates;

    public Point(double... coordinates) {
        this.coordinates = coordinates;
    }

    public static <P extends Point<P>> P newInstance(Class<P> cls, double... coordinates) {
        try {
            return cls.getDeclaredConstructor(double[].class).newInstance(coordinates);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract P create(double... coordinates);

    public P plus(P other) {
        if (coordinates.length != other.coordinates.length) {
            throw new RuntimeException("Dimensions do not match");
        }
        double[] newValues = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; ++i) {
            newValues[i] = coordinates[i] + other.coordinates[i];
        }
        return create(newValues);
    }

    public int getDimension() {
        return coordinates.length;
    }

    public double get(int i) {
        return coordinates[i];
    }

    public double set(int i, double value) {
        return coordinates[i] = value;
    }


    public P multiply(double multiplier) {
        double[] newValues = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; ++i) {
            newValues[i] = multiplier * coordinates[i];
        }
        return create(newValues);
    }

    public P interpolate(P other, double t) {
        return this.multiply(t).plus(other.multiply(1 - t));
    }

    public P over(double divisor) {
        return multiply(1 / divisor);
    }

    public P minus(double...delta) {
        return minus(create(delta));
    }

    public P plus(double...delta) {
        return plus(create(delta));
    }

    public P minus(P delta) {
        return plus(delta.multiply(-1));
    }

    public P copy() {
        return create(coordinates);
    }

    public double distanceTo(P b) {
        return new Vector<>(copy(), b).norm();
    }

    @Override
    public String toString() {
        return "Point{" + Arrays.toString(coordinates) + '}';
    }
}
