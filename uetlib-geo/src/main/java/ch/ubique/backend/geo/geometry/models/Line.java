package ch.ubique.backend.geo.geometry.models;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Line<P extends Point<P>> {

    protected final P start;
    protected final P end;

    public Line(P start, P end) {
        if (start.getDimension() != end.getDimension()) {
            throw new RuntimeException("Dimensions do not match");
        }
        this.start = start;
        this.end = end;
    }

    public P getStart() {
        return start;
    }

    public P getEnd() {
        return end;
    }

    public boolean outOfBounds(P point) {
        if (start.getDimension() != point.getDimension()) {
            throw new RuntimeException("Dimensions do not match");
        }
        for (int i = 0; i < start.getDimension(); ++i) {
            if (!isBetween(point.coordinates[i], start.coordinates[i], end.coordinates[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBetween(double value, double a, double b) {
        return min(a, b) <= value && value <= max(a, b);
    }

    @Override
    public String toString() {
        return String.format("%s, %s", start, end);
    }
}
