package ch.ubique.backend.geo.geometry.models;

import java.util.Objects;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class BoundingBox<P extends Point<P>> {

    private final P min;
    private final P max;

    public BoundingBox(final P min, final P max) {
        if (min.getDimension() != max.getDimension()) {
            throw new RuntimeException("Dimensions do not match");
        }
        this.min = min;
        this.max = max;
    }

    public P getMin() {
        return min;
    }

    public P getMax() {
        return max;
    }

    public Vector<P> getDimension() {
        return new Vector<>(min, max);
    }

    public boolean contains(final P point) {
        if (min.getDimension() != point.getDimension()) {
            throw new RuntimeException("Dimensions do not match");
        }
        for (int i = 0; i < min.getDimension(); ++i) {
            if (point.coordinates[i] < min.coordinates[i]
                    || point.coordinates[i] > max.coordinates[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BoundingBox<?> that = (BoundingBox<?>) o;
        if (!Objects.equals(min, that.min)) return false;
        return Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    public P getCenter() {
        return max.minus(min).over(2);
    }

    @Override
    public String toString() {
        return "BoundingBox{" + "min=" + min + ", max=" + max + '}';
    }
}
