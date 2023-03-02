package ch.ubique.backend.geo.geometry.models;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Polyline {

    List<Point2d> points;

    public Polyline() {
        this.points = new LinkedList<>();
    }

    public void add(Point2d point) {
        points.add(point);
    }

    public void addAll(Collection<Point2d> point) {
        points.addAll(point);
    }

    public List<Point2d> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return points.size() + " points";
    }

    public void setPoints(List<Point2d> points) {
        this.points = points;
    }
}
