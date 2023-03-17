package ch.ubique.backend.geo.geometry.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ch.ubique.backend.geo.geometry.models.BoundingBox;
import ch.ubique.backend.geo.geometry.models.Point;

import org.locationtech.jts.geom.Envelope;

import java.util.Collections;
import java.util.List;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class BoundingBoxHelper {

    private BoundingBoxHelper() {}

    public static <P extends Point<P>> BoundingBox<P> getBoundingBox(final P point) {
        return getBoundingBox(Collections.singletonList(point));
    }

    public static <P extends Point<P>> BoundingBox<P> getBoundingBox(final List<P> points) {
        if (points.isEmpty()) {
            return null;
        }

        final P min = points.get(0).copy();
        final P max = points.get(0).copy();

        for (final P point : points) {
            for (int i = 0; i < min.getDimension(); ++i) {
                min.set(i, min(min.get(i), point.get(i)));
                max.set(i, max(max.get(i), point.get(i)));
            }
        }
        return new BoundingBox<>(min, max);
    }

    public static Envelope getEnvelope(final BoundingBox<?> boundingBox) {
        if (boundingBox == null) {
            return null;
        }

        final var min = boundingBox.getMin();
        final var max = boundingBox.getMax();

        final int x = 0;
        final int y = 1;

        return new Envelope(min.get(x), max.get(x), min.get(y), max.get(y));
    }
}
