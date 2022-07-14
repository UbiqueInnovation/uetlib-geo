package ch.ubique.backend.geo.geometry.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ch.ubique.backend.geo.geometry.models.BoundingBox;
import ch.ubique.backend.geo.geometry.models.Point;
import java.util.Collections;
import java.util.List;

public class BoundingBoxHelper {

    private BoundingBoxHelper() {}

    public static <P extends Point<P>> BoundingBox<P> getBoundingBox(P point) {
        return getBoundingBox(Collections.singletonList(point));
    }

    public static <P extends Point<P>> BoundingBox<P> getBoundingBox(List<P> points) {
        if (points.isEmpty()) {
            return null;
        }

        P min = points.get(0).copy();
        P max = points.get(0).copy();

        for (P point : points) {
            for (int i = 0; i < min.getDimension(); ++i) {
                min.set(i, min(min.get(i), point.get(i)));
                max.set(i, max(max.get(i), point.get(i)));
            }
        }
        return new BoundingBox<>(min, max);
    }
}
