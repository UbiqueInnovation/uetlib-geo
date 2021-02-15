package ch.ubique.backend.geo.geometry.models;

import java.util.LinkedList;
import java.util.List;

public class Line2d extends Line<Point2d> {

    public Line2d(Line<Point2d> line) {
        this(line.start, line.end);
    }

    public Line2d(Point2d start, Point2d end) {
        super(start, end);
    }

    public boolean intersects(Line<Point2d> other) {
        return intersection(other) != null;
    }

    public Point2d intersection(Line<Point2d> other) {
        double a1 = end.getY() - start.getY();
        double b1 = start.getX() - end.getX();
        double c1 = a1 * start.getX() + b1 * start.getY();

        double a2 = other.end.getY() - other.start.getY();
        double b2 = other.start.getX() - other.end.getX();
        double c2 = a2 * other.start.getX() + b2 * other.start.getY();

        double delta = a1 * b2 - a2 * b1;
        double x = (b2 * c1 - b1 * c2) / delta;
        double y = (a1 * c2 - a2 * c1) / delta;

        if (outOfBounds(new Point2d(x, y)) || other.outOfBounds(new Point2d(x, y))) {
            return null;
        }

        double t = (end.getX() - x) / (end.getX() - start.getX());
        return start.interpolate(end, t);
    }

    public List<Tuple<Point2d, Line<Point2d>>> intersections(Polygon2d polygon) {
        return intersections(polygon.getLines());
    }

    public List<Tuple<Point2d, Line<Point2d>>> intersections(List<Line<Point2d>> lines) {
        List<Tuple<Point2d, Line<Point2d>>> intersections = new LinkedList<>();
        for (Line<Point2d> line : lines) {
            Point2d intersection = this.intersection(line);
            if (intersection != null) {
                intersections.add(new Tuple<>(intersection, line));
            }
        }
        return intersections;
    }

    public boolean intersectsBoundary(Polygon<Point2d> polygon) {
        return new Polygon2d(polygon).intersectsBoundary(this);
    }

    public boolean intersects(BoundingBox<Point2d> boundingBox) {
        return new BoundingBox2d(boundingBox).intersects(this);
    }
}