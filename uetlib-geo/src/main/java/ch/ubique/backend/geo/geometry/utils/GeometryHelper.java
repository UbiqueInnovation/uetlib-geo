package ch.ubique.backend.geo.geometry.utils;

import ch.ubique.backend.geo.geometry.models.BoundingBox2d;
import ch.ubique.backend.geo.geometry.models.Line;
import ch.ubique.backend.geo.geometry.models.Line2d;
import ch.ubique.backend.geo.geometry.models.Point;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Polygon2d;
import ch.ubique.backend.geo.geometry.models.Vector;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class GeometryHelper {

    private GeometryHelper() {}

    public static Polygon2d intersection(Polygon2d polygon, BoundingBox2d boundingBox) {

        // Empty box -> no intersection
        if (polygon.getPoints().isEmpty()) {
            return null;
        }

        // Find out where the points are in relation to the box
        boolean anyPointInsideBox = false;
        boolean anyPointOutsideBox = false;
        for (Point2d point : polygon.getPoints()) {
            if (boundingBox.contains(point)) {
                anyPointInsideBox = true;
            } else {
                anyPointOutsideBox = true;
            }
        }
        boolean allPointsInsideBox = anyPointInsideBox && !anyPointOutsideBox;
        boolean allPointsOutsideBox = anyPointOutsideBox && !anyPointInsideBox;

        // BoundingBox contains entire polygon
        if (allPointsInsideBox) {
            return polygon;
        }

        if (allPointsOutsideBox) {
            // Polygon contains entire bounding box
            if (polygon.contains(boundingBox.getCenter())) {
                return boundingBox.toPolygon();
            }
            // No intersection
            return null;
        }

        // Edges of Polygon and BoundingBox overlap
        // Build new Polygon
        final var newPolygon = new Polygon2d();
        Line<Point2d> lastIntersection = null;

        for (Line<Point2d> line : polygon.getLines()) {
            final var boundingBoxLines = boundingBox.toPolygon().getLines();
            final var intersections = new Line2d(line).intersections(boundingBoxLines);

            if (intersections.isEmpty()) {
                continue;
            }

            final var start = line.getStart();
            final var end = line.getEnd();

            // Add corners if needed
            BiConsumer<Line<Point2d>, Line<Point2d>> addCorners =
                    (last, current) -> {
                        if (last != null && last != current) {
                            boolean forwards = polygon.contains(last.getEnd());
                            int firstIndex = boundingBoxLines.indexOf(last);
                            int lastIndex = boundingBoxLines.indexOf(current);
                            if (forwards) {
                                for (int i = firstIndex; i != lastIndex; i = (i + 1) % 4) {
                                    newPolygon.getPoints().add(polygon.getLines().get(i).getEnd());
                                }
                            } else {
                                for (int i = lastIndex; i != firstIndex; i = (i + 1) % 4) {
                                    newPolygon.getPoints().add(polygon.getLines().get(i).getEnd());
                                }
                            }
                        }
                    };

            if (boundingBox.contains(end)) {
                newPolygon.getPoints().add(end);
            } else {
                // Outgoing line
                final var closest =
                        intersections.stream()
                                .min(Comparator.comparingDouble(t -> end.distanceTo(t.getLeft())))
                                .orElseThrow();
                newPolygon.getPoints().add(closest.getLeft());

                // add corners if needed
                final var currentIntersection = closest.getRight();
                addCorners.accept(lastIntersection, currentIntersection);
                lastIntersection = currentIntersection;
            }

            if (boundingBox.contains(start)) {
                if (!newPolygon.contains(start)) {
                    newPolygon.getPoints().add(start);
                }
            } else {
                // Incoming line
                final var closest =
                        intersections.stream()
                                .min(Comparator.comparingDouble(t -> start.distanceTo(t.getLeft())))
                                .orElseThrow();
                newPolygon.getPoints().add(closest.getLeft());

                // add corners if needed
                final var currentIntersection = closest.getRight();
                addCorners.accept(lastIntersection, currentIntersection);
                lastIntersection = currentIntersection;
            }
        }
        return newPolygon;
    }

    public static <P extends Point<P>> List<P> simplify(
            final List<P> points, double minAngle, double minDistance) {

        if (points.size() <= 2) {
            return points;
        }

        Iterator<P> oldPoints = points.iterator();
        List<P> newPoints = new LinkedList<>();

        P start;
        P middle = null;
        P end = null;
        while (oldPoints.hasNext()) {

            // Advance
            start = middle;
            middle = end;
            end = oldPoints.next();

            // First point
            if (middle == null) {
                newPoints.add(end);
            }

            // Not all points available
            if (start == null || middle == null) {
                continue;
            }

            // Get distance and angle
            double distance = start.distanceTo(middle);
            double angle = angle(start, middle, end);

            // Add point if distance and angle are big enough
            if (distance >= minDistance && angle >= minAngle) {
                newPoints.add(middle);
            }
        }
        // Preserve end
        newPoints.add(end);

        return newPoints;
    }

    public static <P extends Point<P>> double angle(P start, P middle, P end) {
        return new Vector<>(start, middle).angle(new Vector<>(middle, end));
    }

}
