package ch.ubique.backend.geo.geometry.models;

import java.util.Arrays;

/**
 * @deprecated Please use JTS instead.
 *     <p>This class will be removed with version 2.0.0 and should no longer be used
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class BoundingBox2d extends BoundingBox<Point2d> {

    public BoundingBox2d(final BoundingBox<Point2d> box) {
        this(box.getMin(), box.getMax());
    }

    public BoundingBox2d(final Point2d min, final Point2d max) {
        super(min, max);
    }

    public double getWidth() {
        return getMax().getX() - getMin().getX();
    }

    public double getHeight() {
        return getMax().getY() - getMin().getY();
    }

    public Polygon2d toPolygon() {
        final Point2d bottomLeft = getMin().copy();
        final Point2d bottomRight = bottomLeft.copy();
        bottomRight.setX(getMax().getX());
        final Point2d topRight = getMax().copy();
        final Point2d topLeft = topRight.copy();
        topLeft.setX(getMin().getX());
        return new Polygon2d(Arrays.asList(bottomLeft, bottomRight, topLeft, topRight));
    }

    public boolean intersects(final Line<Point2d> line) {
        // Line ends on polygon
        if (contains(line.getStart()) || contains(line.getEnd())) {
            return true;
        }
        // Line crosses one boundary
        return toPolygon().intersectsBoundary(line);
    }
}
