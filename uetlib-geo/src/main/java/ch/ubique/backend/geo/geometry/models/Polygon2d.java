package ch.ubique.backend.geo.geometry.models;

import java.util.List;

@Deprecated(since = "1.3.0", forRemoval = true)
public class Polygon2d extends Polygon<Point2d> {

    public Polygon2d(Polygon<Point2d> polygon) {
        this(polygon.getPoints());
    }

    public Polygon2d(List<Point2d> points) {
        super(points);
    }

    public Polygon2d() {
        super();
    }

    public int countBoundaryIntersections(Line<Point2d> line) {
        return new Line2d(line).intersections(this).size();
    }

    public boolean intersectsBoundary(Line<Point2d> line) {
        return countBoundaryIntersections(line) > 0;
    }

    public boolean contains(Point2d point) {
        BoundingBox2d boundingBox = new BoundingBox2d(getBoundingBox());
        Point2d rayOrigin = boundingBox.getMin().minus(0.1 * boundingBox.getWidth());
        Line<Point2d> ray = new Line<>(rayOrigin, point);
        int count = countBoundaryIntersections(ray) % 2;
        // Odd count => inside => point contained
        return (count % 2) == 1;
    }
}
