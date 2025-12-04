package ch.ubique.backend.geo.coordinate.util;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

class JtsUtilTest {

    private final GeometryFactory factory = new GeometryFactory();
    private final WKTReader reader = new WKTReader(factory);

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

    @Test
    void representativeCoordinate_shouldReturnCoordinate_whenPoint() throws ParseException {
        final var point = (Point) reader.read("POINT (10 20)");
        final var result = JtsUtil.representativeCoordinate(point);
        assertEquals(new Coordinate(10, 20), result);
    }

    @Test
    void representativeCoordinate_shouldReturnMidpoint_whenLineString() throws ParseException {
        final var lineString = (LineString) reader.read("LINESTRING (0 0, 10 10)");
        final var result = JtsUtil.representativeCoordinate(lineString);
        assertEquals(new Coordinate(5, 5), result);
    }

    @Test
    void representativeCoordinate_shouldReturnInsidePoint_whenPolygon() throws ParseException {
        final var polygon = (Polygon) reader.read("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
        final var result = JtsUtil.representativeCoordinate(polygon);

        assertTrue(polygon.contains(factory.createPoint(result)), "Point should be inside polygon");
        // MaximumInscribedCircle center for a square is roughly the center
        assertEquals(5.0, result.x, 0.1);
        assertEquals(5.0, result.y, 0.1);
    }

    @Test
    void representativeCoordinate_shouldReturnCentroid_whenMultiPolygonCentroidIsInside()
            throws ParseException {
        // A large square and a tiny disjoint square.
        // Centroid should be dominated by the large square and fall inside it.
        final var multiPolygon =
                (MultiPolygon)
                        reader.read(
                                "MULTIPOLYGON (((0 0, 100 0, 100 100, 0 100, 0 0)), ((200 0, 201 0, 201 1, 200 1, 200 0)))");

        final var result = JtsUtil.representativeCoordinate(multiPolygon);

        assertTrue(
                multiPolygon.contains(factory.createPoint(result)),
                "Result should be inside one of the polygons");
        // The centroid of the combined geometry should be inside the large polygon, so it returns
        // that directly
        assertTrue(multiPolygon.contains(multiPolygon.getCentroid()));
        assertEquals(multiPolygon.getCentroid().getCoordinate(), result);
    }

    @Test
    void
    representativeCoordinate_shouldReturnClosestChildRepresentative_whenMultiPolygonCentroidIsOutside()
                    throws ParseException {
        // Two squares separated by a gap. Centroid falls in the gap.
        final var multiPolygon =
                (MultiPolygon)
                        reader.read(
                                "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), ((20 0, 30 0, 30 10, 20 10, 20 0)))");

        final var centroid = multiPolygon.getCentroid().getCoordinate(); // Should be at (15, 5)
        assertFalse(
                multiPolygon.contains(multiPolygon.getCentroid()),
                "Centroid should be outside for this test case");

        final var result = JtsUtil.representativeCoordinate(multiPolygon);

        assertTrue(
                multiPolygon.contains(factory.createPoint(result)),
                "Result should be inside geometry");
        // It should pick one of the centers (5,5) or (25,5). Both are distance 10 from (15,5).
        final var dist = result.distance(centroid);
        assertEquals(10.0, dist, 0.1);
    }

    @Test
    void representativeCoordinate_shouldProjectToSegment_whenMultiLineString()
            throws ParseException {
        // Two parallel lines. Centroid is exactly in between at (5, 5).
        final var multiLineString =
                (MultiLineString) reader.read("MULTILINESTRING ((0 0, 10 0), (0 10, 10 10))");

        final var result = JtsUtil.representativeCoordinate(multiLineString);

        // It should project (5,5) to either (5,0) or (5,10)
        assertTrue(
                multiLineString.distance(factory.createPoint(result)) < 1.0e-9,
                "Result should be on one of the lines");
        assertEquals(5.0, result.x, 0.001);
        assertTrue(Math.abs(result.y - 0.0) < 0.001 || Math.abs(result.y - 10.0) < 0.001);
    }

    @Test
    void representativeCoordinate_shouldSelectClosestGeometry_whenGeometryCollection()
            throws ParseException {
        // A point at (0,0) and a point at (100,100). Centroid at (50,50).
        final var collection =
                (GeometryCollection)
                        reader.read("GEOMETRYCOLLECTION (POINT (0 0), POINT (100 100))");

        final var result = JtsUtil.representativeCoordinate(collection);

        // Should pick one of the points as they are the representative coords of the children
        assertTrue(result.equals(new Coordinate(0, 0)) || result.equals(new Coordinate(100, 100)));
    }

    @Test
    void representativeCoordinateShouldThrowExceptionWhenGeometryEmpty() throws ParseException {
        final var empty = reader.read("POINT EMPTY");
        assertThrows(NoSuchElementException.class, () -> JtsUtil.representativeCoordinate(empty));
    }

    @Test
    void representativeCoordinate_shouldThrowException_whenGeometryNull() {
        assertThrows(NoSuchElementException.class, () -> JtsUtil.representativeCoordinate(null));
    }
}
