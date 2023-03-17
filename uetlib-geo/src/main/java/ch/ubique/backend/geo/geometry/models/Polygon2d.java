package ch.ubique.backend.geo.geometry.models;

import java.util.List;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Polygon2d extends Polygon<Point2d> {

    public Polygon2d(final Polygon<Point2d> polygon) {
        this(polygon.getPoints());
    }

    public Polygon2d(final List<Point2d> points) {
        super(points);
    }

    public int countBoundaryIntersections(final Line<Point2d> line) {
        return new Line2d(line).intersections(this).size();
    }

    public boolean intersectsBoundary(final Line<Point2d> line) {
        return countBoundaryIntersections(line) > 0;
    }

    public boolean contains(final Point2d point) {
        final BoundingBox2d boundingBox = new BoundingBox2d(getBoundingBox());
        final Point2d rayOrigin = boundingBox.getMin().minus(0.1 * boundingBox.getWidth());
        final Line<Point2d> ray = new Line<>(rayOrigin, point);
        final int count = countBoundaryIntersections(ray) % 2;
        // Odd count => inside => point contained
        return (count % 2) == 1;
    }
}
