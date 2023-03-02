package ch.ubique.backend.geo.geometry.models;

import ch.ubique.backend.geo.geometry.utils.BoundingBoxHelper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class Polygon<P extends Point<P>> {

    private final List<P> points;

    public Polygon(List<P> points) {
        this.points = points;
    }

    public Polygon() {
        this.points = new LinkedList<>();
    }

    public BoundingBox<P> getBoundingBox() {
        return BoundingBoxHelper.getBoundingBox(points);
    }

    public List<P> getPoints() {
        return points;
    }

    public List<Line<P>> getLines() {
        List<Line<P>> lines = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); ++i) {
            lines.add(new Line<>(points.get(i), points.get((i + 1) % points.size())));
        }
        return lines;
    }

    @Override
    public String toString() {
        return points.size() + " points";
    }
}
