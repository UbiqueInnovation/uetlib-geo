package ch.ubique.backend.geo.geometry.models;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Line2d extends Line<Point2d> {

    public Line2d(final Line<Point2d> line) {
        this(line.start, line.end);
    }

    public Line2d(final Point2d start, final Point2d end) {
        super(start, end);
    }

    public boolean intersects(final Line<Point2d> other) {
        return intersection(other) != null;
    }

    public Point2d intersection(final Line<Point2d> other) {
        final double a1 = end.getY() - start.getY();
        final double b1 = start.getX() - end.getX();
        final double c1 = a1 * start.getX() + b1 * start.getY();

        final double a2 = other.end.getY() - other.start.getY();
        final double b2 = other.start.getX() - other.end.getX();
        final double c2 = a2 * other.start.getX() + b2 * other.start.getY();

        final double delta = a1 * b2 - a2 * b1;
        final double x = (b2 * c1 - b1 * c2) / delta;
        final double y = (a1 * c2 - a2 * c1) / delta;

        if (outOfBounds(new Point2d(x, y)) || other.outOfBounds(new Point2d(x, y))) {
            return null;
        }

        final double t = (end.getX() - x) / (end.getX() - start.getX());
        return start.interpolate(end, t);
    }

    public List<Pair<Point2d, Line<Point2d>>> intersections(final Polygon2d polygon) {
        return intersections(polygon.getLines());
    }

    public List<Pair<Point2d, Line<Point2d>>> intersections(final List<Line<Point2d>> lines) {
        final List<Pair<Point2d, Line<Point2d>>> intersections = new LinkedList<>();
        for (final Line<Point2d> line : lines) {
            final Point2d intersection = intersection(line);
            if (intersection != null) {
                intersections.add(Pair.of(intersection, line));
            }
        }
        return intersections;
    }

    public boolean intersectsBoundary(final Polygon<Point2d> polygon) {
        return new Polygon2d(polygon).intersectsBoundary(this);
    }

    public boolean intersects(final BoundingBox<Point2d> boundingBox) {
        return new BoundingBox2d(boundingBox).intersects(this);
    }
}
