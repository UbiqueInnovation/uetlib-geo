package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class JtsUtilTest {

    @Test
    void toJtsMultiPoint() {
        final var p1 = new WGS84Coordinate(1, 2).toJtsPoint();
        final var p2 = new WGS84Coordinate(3, 4).toJtsPoint();

        final var expected = p1.getFactory().createMultiPoint(new Point[] {p1, p2});

        final var multiPointFromSet = JtsUtil.toJtsMultiPoint(Set.of(p1, p2));
        assertTrue(expected.equalsTopo(multiPointFromSet));
        assertEquals(CoordinateSystems.WGS84.srid(), multiPointFromSet.getSRID());

        final var multiPointFromStream = JtsUtil.toJtsMultiPoint(Stream.of(p1, p2));
        assertEquals(expected, multiPointFromStream);
        assertEquals(CoordinateSystems.WGS84.srid(), multiPointFromStream.getSRID());

        assertEquals(p1.getFactory().createMultiPoint(), JtsUtil.toJtsMultiPoint(List.of()));
        assertEquals(p1.getFactory().createMultiPoint(), JtsUtil.toJtsMultiPoint(Stream.of()));
    }

    @Test
    void flattenGeometry() {
        final var p = new LV03Coordinate(1, 2).toJtsPoint();
        final var srid = CoordinateSystems.LV03.srid();
        final var geometryFactory = p.getFactory();

        final var nested = geometryFactory.createGeometryCollection(new Geometry[] {p});
        assertEquals(List.of(p), JtsUtil.flattenGeometry(nested));
        assertTrue(JtsUtil.flattenGeometry(nested).stream().allMatch(g -> g.getSRID() == srid));

        final var doubleNested = geometryFactory.createGeometryCollection(new Geometry[] {nested});
        assertEquals(List.of(p), JtsUtil.flattenGeometry(doubleNested));
        assertTrue(
                JtsUtil.flattenGeometry(doubleNested).stream().allMatch(g -> g.getSRID() == srid));

        final var differentLevels =
                geometryFactory.createGeometryCollection(new Geometry[] {p, nested, doubleNested});
        assertEquals(List.of(p, p, p), JtsUtil.flattenGeometry(differentLevels));
        assertTrue(
                JtsUtil.flattenGeometry(differentLevels).stream()
                        .allMatch(g -> g.getSRID() == srid));
    }

    @Test
    void unwrapIfGeometryCollectionHasOnlyOneGeometry() {
        final var p1 = new WGS84Coordinate(1, 2).toJtsPoint();
        final var p2 = new WGS84Coordinate(3, 4).toJtsPoint();
        final var srid = CoordinateSystems.WGS84.srid();
        final var geometryFactory = p1.getFactory();

        final var unwrappedPlain = JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(p1);
        assertEquals(p1, unwrappedPlain);
        assertEquals(srid, unwrappedPlain.getSRID());

        final var bothPoints = geometryFactory.createGeometryCollection(new Geometry[] {p1, p2});
        assertEquals(bothPoints, JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(bothPoints));
        assertEquals(srid, bothPoints.getSRID());

        final var nested = geometryFactory.createGeometryCollection(new Geometry[] {p1});
        final var unwrappedNested = JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(nested);
        assertEquals(p1, unwrappedNested);
        assertEquals(srid, unwrappedNested.getSRID());

        final var doubleNested = geometryFactory.createGeometryCollection(new Geometry[] {nested});
        final var unwrappedDoubleNested =
                JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(doubleNested);
        assertEquals(p1, unwrappedDoubleNested);
        assertEquals(srid, unwrappedDoubleNested.getSRID());

        final var nestedBothPoints =
                geometryFactory.createGeometryCollection(new Geometry[] {bothPoints});
        final var unwrappedNestedBothPoints =
                JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(nestedBothPoints);
        assertEquals(bothPoints, unwrappedNestedBothPoints);
        assertEquals(srid, unwrappedNestedBothPoints.getSRID());

        final var empty = geometryFactory.createGeometryCollection();
        final var unwrappedEmpty = JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(empty);
        assertEquals(empty, unwrappedEmpty);
        assertEquals(srid, unwrappedEmpty.getSRID());

        final var nestedEmpty = geometryFactory.createGeometryCollection(new Geometry[] {empty});
        final var unwrappedNestedEmpty =
                JtsUtil.unwrapIfGeometryCollectionHasOnlyOneGeometry(nestedEmpty);
        assertEquals(empty, unwrappedNestedEmpty);
        assertEquals(srid, unwrappedNestedEmpty.getSRID());
    }

    @Test
    void midPoint() {
        assertNull(JtsUtil.midPoint(l()));
        assertEquals(c(10, 0), JtsUtil.midPoint(l(c(0, 0), c(20, 0))));
        assertEquals(c(10, 0), JtsUtil.midPoint(l(c(0, 0), c(10, 0), c(20, 0))));
        assertEquals(
                c(10, 0),
                JtsUtil.midPoint(l(c(0, 0), c(1, 0), c(2, 0), c(3, 0), c(4, 0), c(20, 0))));
        assertEquals(c(10, 10), JtsUtil.midPoint(l(c(0, 0), c(20, 20))));
        assertEquals(c(10, 10), JtsUtil.midPoint(l(c(0, 0), c(20, 20))));
        assertEquals(c(10, 10), JtsUtil.midPoint(l(c(0, 0), c(10, 0), c(10, 20), c(20, 20))));
    }

    @Test
    void closestSegment() {
        assertNull(JtsUtil.closestSegment(l(), c(0, 0)));
        assertEquals(s(c(0, 0), c(10, 10)), JtsUtil.midPointSegment(l(c(0, 0), c(10, 10))));
        assertEquals(c(10, 0), JtsUtil.midPoint(l(c(0, 0), c(10, 0), c(20, 0))));

        final var testLine = l(c(0, 0), c(1, 0), c(2, 0), c(3, 0), c(4, 0), c(20, 0));
        assertEquals(s(c(0, 0), c(1, 0)), JtsUtil.closestSegment(testLine, c(0.1, 0)));
        assertEquals(s(c(1, 0), c(2, 0)), JtsUtil.closestSegment(testLine, c(1.4, 100)));
        assertEquals(s(c(2, 0), c(3, 0)), JtsUtil.closestSegment(testLine, c(2.1, 0)));
        assertEquals(s(c(3, 0), c(4, 0)), JtsUtil.closestSegment(testLine, c(3.1, 0)));
        assertEquals(s(c(4, 0), c(20, 0)), JtsUtil.closestSegment(testLine, c(4.1, -40)));
        assertEquals(s(c(4, 0), c(20, 0)), JtsUtil.closestSegment(testLine, c(29, 0)));
        assertEquals(s(c(4, 0), c(20, 0)), JtsUtil.midPointSegment(testLine));
    }

    private Coordinate c(final double x, final double y) {
        return new Coordinate(x, y);
    }

    private LineString l(final Coordinate... coordinates) {
        return new GeometryFactory().createLineString(coordinates);
    }

    private LineSegment s(final Coordinate a, final Coordinate b) {
        return new LineSegment(a, b);
    }
}
