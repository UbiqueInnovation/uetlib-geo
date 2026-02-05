package ch.ubique.backend.geo.coordinate.util;

import static org.locationtech.jts.noding.SegmentStringUtil.extractSegmentStrings;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.cs.AxisDirection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class JtsUtil {

    private static final Logger logger = LoggerFactory.getLogger(JtsUtil.class);

    private JtsUtil() {}

    @NonNull
    public static MultiPoint toJtsMultiPoint(@NonNull final Collection<Point> points) {
        return factoryFromGeometryCollection(points).createMultiPoint(points.toArray(Point[]::new));
    }

    @NonNull
    public static MultiPoint toJtsMultiPoint(@NonNull final Stream<Point> points) {
        final var array = points.toArray(Point[]::new);
        return factoryFromGeometryCollection(array).createMultiPoint(array);
    }

    @NonNull
    public static MultiLineString toJtsMultiLineString(
            @NonNull final Collection<LineString> lineStrings) {
        return factoryFromGeometryCollection(lineStrings)
                .createMultiLineString(lineStrings.toArray(LineString[]::new));
    }

    @NonNull
    public static MultiLineString toJtsMultiLineString(
            @NonNull final Stream<LineString> lineStrings) {
        final var array = lineStrings.toArray(LineString[]::new);
        return factoryFromGeometryCollection(array).createMultiLineString(array);
    }

    @NonNull
    public static MultiPolygon toJtsMultiPolygon(@NonNull final Collection<Polygon> polygons) {
        return factoryFromGeometryCollection(polygons)
                .createMultiPolygon(polygons.toArray(Polygon[]::new));
    }

    @NonNull
    public static MultiPolygon toJtsMultiPolygon(@NonNull final Stream<Polygon> polygons) {
        final var array = polygons.toArray(Polygon[]::new);
        return factoryFromGeometryCollection(array).createMultiPolygon(array);
    }

    /**
     * Converts a stream of geometries into a MultiGeometry object.
     *
     * <ul>
     *   <li>If all geometries are of the same type (e.g., Point, LineString, Polygon), the method
     *       creates a corresponding MultiPoint, MultiLineString, or MultiPolygon.
     *   <li>Otherwise, it creates a generic GeometryCollection.
     * </ul>
     */
    public static GeometryCollection toMultiGeometry(final Stream<Geometry> geometries) {
        final var array = flattenGeometries(geometries).toArray(Geometry[]::new);
        final var factory = factoryFromGeometryCollection(array);
        if (Arrays.stream(array).allMatch(Point.class::isInstance)) {
            return factory.createMultiPoint(Arrays.copyOf(array, array.length, Point[].class));
        }
        if (Arrays.stream(array).allMatch(LineString.class::isInstance)) {
            return factory.createMultiLineString(
                    Arrays.copyOf(array, array.length, LineString[].class));
        }
        if (Arrays.stream(array).allMatch(Polygon.class::isInstance)) {
            return factory.createMultiPolygon(Arrays.copyOf(array, array.length, Polygon[].class));
        }
        return factory.createGeometryCollection(array);
    }

    @NonNull
    public static GeometryCollection toGeometryCollection(
            @NonNull final Collection<Geometry> geometries) {
        return factoryFromGeometryCollection(geometries)
                .createGeometryCollection(geometries.toArray(Geometry[]::new));
    }

    @NonNull
    public static GeometryCollection toGeometryCollection(
            @NonNull final Stream<Geometry> geometries) {
        final var array = geometries.toArray(Geometry[]::new);
        return factoryFromGeometryCollection(array).createGeometryCollection(array);
    }

    public static GeometryFactory factoryFromGeometryCollection(
            @NonNull final Collection<? extends Geometry> geometries) {
        final var geometryMaybe = geometries.stream().findAny();
        return geometryMaybe.map(Geometry::getFactory).orElseGet(GeometryFactory::new);
    }

    public static GeometryFactory factoryFromGeometryCollection(
            @NonNull final Geometry[] geometries) {
        if (geometries.length > 0) {
            return geometries[0].getFactory();
        }
        return new GeometryFactory();
    }

    public static CoordinateReferenceSystem getCrs(final Geometry geometry) {
        return getCrs(geometry.getSRID());
    }

    public static CoordinateReferenceSystem getCrs(final int srid) {
        if (srid == 0) {
            throw new WrappedException(
                    new MissingInfoException(
                            "The Spatial Reference ID (SRID) is not set for the given geometry."));
        }
        try {
            if (srid == 4326) {
                // the default for EPSG:4326 is latitude first. our JtsConverter as well as PostGIS
                // works with longitude first, so this will be our default
                return CRS.decode("EPSG:4326", true);
            }
            return CRS.decode("EPSG:" + srid);
        } catch (final FactoryException e) {
            throw new WrappedException(
                    e); // rethrow as runtime exception. no way for caller to handle this
        }
    }

    public static List<LineSegment> segments(final LineString lineString) {
        return segmentsStream(lineString).toList();
    }

    public static Stream<LineSegment> segmentsStream(final LineString lineString) {
        final var coordinates = lineString.getCoordinates();
        return IntStream.range(0, coordinates.length - 1)
                .mapToObj(i -> new LineSegment(coordinates[i], coordinates[i + 1]));
    }

    @NonNull
    public static Coordinate midPoint(final @NonNull LineString lineString) {
        final var length = lineString.getLength();
        if (length <= 0) {
            return lineString.getCoordinate();
        }
        final var midpointDistance = length / 2;
        var currentDistance = 0.0;
        for (final var segment : segments(lineString)) {
            final var segmentLength = segment.getLength();
            if (currentDistance + segmentLength >= midpointDistance) {
                final var fraction = (midpointDistance - currentDistance) / segmentLength;
                return segment.pointAlong(fraction);
            }
            currentDistance += segmentLength;
        }
        throw new IllegalStateException("Unreachable.");
    }

    @Nullable
    public static LineSegment closestSegment(
            final @NonNull LineString lineString, final @NonNull Coordinate coordinate) {
        return segmentsStream(lineString)
                .min(Comparator.comparingDouble(segment -> segment.distance(coordinate)))
                .orElse(null);
    }

    @Nullable
    public static LineSegment midPointSegment(final @NonNull LineString lineString) {
        return closestSegment(lineString, midPoint(lineString));
    }

    /***
     * @return
     *     <li>For a non-multi geometry, a list with just the input geometry is returned.
     *     <li>For a multi geometry, this returns a list of all child geometries contained in the
     *         given geometry.
     *         <p>Note: this method is recursive, i.e., if a child geometry is again a multi
     *         geometry, the child geometries are again extracted.
     */
    public static List<Geometry> flattenGeometry(final Geometry geometry) {
        return flatGeometriesStream(geometry).toList();
    }

    /**
     * Flattens a geometry by recursively extracting all child geometries and returning them as a
     * stream.
     *
     * @param geometry the geometry to be flattened
     * @return a stream of flattened geometries
     */
    public static Stream<Geometry> flatGeometriesStream(final Geometry geometry) {
        if (!(geometry instanceof GeometryCollection)) {
            return getGeometriesAsStream(geometry);
        }

        // Recursively call this function until all child geometries are flattened
        return getGeometriesAsStream(geometry).flatMap(JtsUtil::flatGeometriesStream);
    }

    /**
     * Flattens a geometry by recursively extracting all child geometries and returning them as a
     * stream.
     *
     * @param geometries the geometries to be flattened
     * @return a stream of flattened geometries
     */
    public static Stream<Geometry> flatGeometriesStream(final Collection<Geometry> geometries) {
        return flatGeometriesStream(geometries.stream());
    }

    /**
     * Flattens a geometry by recursively extracting all child geometries and returning them as a
     * stream.
     *
     * @param geometries the geometries to be flattened
     * @return a stream of flattened geometries
     */
    public static Stream<Geometry> flatGeometriesStream(final Stream<Geometry> geometries) {
        return geometries.flatMap(JtsUtil::flatGeometriesStream);
    }

    /**
     * Flattens a collection of geometries by recursively extracting all child geometries
     *
     * @param geometries a collection of geometries to be flattened
     * @return a collection of flattened geometries
     */
    public static List<Geometry> flattenGeometries(final Collection<Geometry> geometries) {
        return flatGeometriesStream(geometries).toList();
    }

    /**
     * Flattens a stream of geometries by recursively extracting all child geometries
     *
     * @param geometries a stream of geometries to be flattened
     * @return a collection of flattened geometries
     */
    public static List<Geometry> flattenGeometries(final Stream<Geometry> geometries) {
        return flatGeometriesStream(geometries).toList();
    }

    /**
     * Retrieves the individual geometries contained within a Geometry object as a stream. *
     *
     * @param geometry the Geometry object from which to extract the geometries
     * @return a stream of the individual geometries within the provided Geometry object
     */
    @NonNull
    public static Stream<Geometry> getGeometriesAsStream(final Geometry geometry) {
        return IntStream.range(0, geometry.getNumGeometries()).mapToObj(geometry::getGeometryN);
    }

    @NonNull
    public static Stream<Point> getPointsStream(final MultiPoint multiPoint) {
        return getGeometriesAsStream(multiPoint).map(Point.class::cast);
    }

    @NonNull
    public static Stream<LineString> getLinesStream(final MultiLineString multiLineString) {
        return getGeometriesAsStream(multiLineString).map(LineString.class::cast);
    }

    @NonNull
    public static Stream<Polygon> getPolygonsStream(final MultiPolygon multiPolygon) {
        return getGeometriesAsStream(multiPolygon).map(Polygon.class::cast);
    }

    @NonNull
    public static Stream<LinearRing> getInteriorRingsStream(final Polygon polygon) {
        return IntStream.range(0, polygon.getNumInteriorRing()).mapToObj(polygon::getInteriorRingN);
    }

    @NonNull
    public static Collection<Geometry> getGeometries(final Geometry geometry) {
        return getGeometriesAsStream(geometry).toList();
    }

    /**
     * @return
     *     <li>For a non-multi geometry, an iterator with just the input geometry is returned.
     *     <li>For a multi geometry, this returns an iterator of all child geometries contained in
     *         the given geometry.
     *         <p>Note: this method is recursive, i.e., if a child geometry is again a multi
     *         geometry, the child geometries are again extracted.
     */
    public static Iterator<Geometry> getFlatGeometriesIterator(final Geometry geometry) {
        return flatGeometriesStream(geometry).iterator();
    }

    @NonNull
    public static Geometry tryToGetValidGeometry(@NonNull final Geometry geometry)
            throws UnfixableGeometryException {
        final var validator = new IsValidOp(geometry);

        if (validator.isValid()) {
            return geometry;
        }

        logger.debug(
                "Attempting to fix invalid geometry. Validation error = {}",
                validator.getValidationError());
        final var fixedGeometry = GeometryFixer.fix(geometry);
        // GeometryFixer.fix doesnt update srid
        updateSrid(fixedGeometry, geometry.getSRID());

        final var fixedValidator = new IsValidOp(fixedGeometry);
        if (fixedValidator.isValid()) {
            logger.debug("Successfully fixed geometry.");
            return fixedGeometry;
        }

        throw new UnfixableGeometryException(fixedValidator.getValidationError());
    }

    public static void updateSrid(final Geometry geometry, final int srid) {
        // We need to recreate the geometry, otherwise it has the wrong geometry factory
        geometryFactoryForSrid(srid).createGeometry(geometry);
    }

    public static GeometryFactory geometryFactoryForSrid(final int srid) {
        return new GeometryFactory(new PrecisionModel(), srid);
    }

    public static void throwIfCoordinateSystemNotEastNorth(final CoordinateReferenceSystem crs) {
        final var coordinateSystem = crs.getCoordinateSystem();

        if (!coordinateSystem.getAxis(0).getDirection().equals(AxisDirection.EAST)) {
            throw new UnsupportedOperationException(
                    "Only coordinate systems with x-axis direction east are supported.");
        }

        if (!coordinateSystem.getAxis(1).getDirection().equals(AxisDirection.NORTH)) {
            throw new UnsupportedOperationException(
                    "Only coordinate systems with y-axis direction north are supported.");
        }
    }

    public static Envelope transformEnvelope(
            final ReferencedEnvelope outputEnvelope, final CoordinateReferenceSystem targetCrs) {
        if (outputEnvelope.getCoordinateReferenceSystem().equals(targetCrs)) {
            return outputEnvelope;
        } else {
            try {
                return outputEnvelope.transform(targetCrs, true);
            } catch (final Exception e) {
                // rethrow as runtime exception. no way for caller to handle this
                throw new WrappedException(e);
            }
        }
    }

    public static Geometry snapSegments(final Geometry geometry, final double snapTolerance) {
        final var noder = new SnappingNoder(snapTolerance);
        noder.computeNodes(extractSegmentStrings(geometry));
        return SegmentStringUtil.toGeometry(noder.getNodedSubstrings(), geometry.getFactory());
    }

    public static Geometry mergeLines(final Geometry geometry) {
        return mergeLines(Stream.of(geometry));
    }

    public static Geometry mergeLines(final Collection<Geometry> geometries) {
        return mergeLines(geometries.stream());
    }

    public static Geometry mergeLines(final Stream<Geometry> geometryStream) {
        final var merged = toJtsMultiLineString(mergeLinesStream(geometryStream));
        return unwrapIfGeometryCollectionHasOnlyOneGeometry(merged);
    }

    public static Stream<LineString> mergeLinesStream(final Stream<Geometry> geometryStream) {
        final var lineMerger = new LineMerger();
        geometryStream.forEach(lineMerger::add);
        //noinspection unchecked
        return lineMerger.getMergedLineStrings().stream();
    }

    public static Geometry unwrapIfGeometryCollectionHasOnlyOneGeometry(final Geometry geometry) {
        if (geometry instanceof GeometryCollection && geometry.getNumGeometries() == 1) {
            return unwrapIfGeometryCollectionHasOnlyOneGeometry(geometry.getGeometryN(0));
        }
        return geometry;
    }

    public static Geometry snapAndMergeLines(final Geometry geometry, final double snapTolerance) {

        // Snap nodes of lines
        final var snapped = JtsUtil.snapSegments(geometry, snapTolerance);

        // Self-union removes duplicate segments
        final var snappedWithoutDuplicates = snapped.union();

        // Merge lines
        return JtsUtil.mergeLines(snappedWithoutDuplicates);
    }

    /**
     * @return A referenced envelope that is the smallest axis aligned bounding box containing all
     *     geometries
     */
    public static ReferencedEnvelope getEnvelope(final Collection<Geometry> geometries) {

        if (geometries == null || geometries.isEmpty()) {
            throw new NoSuchElementException("No geometries provided.");
        }

        final var geometriesArray = geometries.toArray(Geometry[]::new);
        final var anyGeometry = geometriesArray[0];
        final var geometryCollection =
                new GeometryCollection(geometriesArray, anyGeometry.getFactory());

        return getEnvelope(geometryCollection);
    }

    public static <C extends ch.ubique.backend.geo.coordinate.models.Coordinate>
            ReferencedEnvelope getEnvelope(@NonNull final C coordinate) {
        requireNonNull(coordinate);
        return new ReferencedEnvelope(
                new Envelope(coordinate.toJts()),
                CoordinateSystems.forClass(coordinate.getClass()).crs());
    }

    public static ReferencedEnvelope getEnvelope(final Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            throw new NoSuchElementException("No/empty geometry provided.");
        }
        return new ReferencedEnvelope(geometry.getEnvelopeInternal(), getCrs(geometry));
    }

    public static Envelope getEnvelope(final LineSegment lineSegment) {
        return new Envelope(lineSegment.p0, lineSegment.p1);
    }

    public static double zDistance(final Coordinate a, final Coordinate b) {
        if (Double.isNaN(a.getZ()) || Double.isNaN(b.getZ())) {
            return 0;
        }
        return Math.abs(b.getZ() - a.getZ());
    }

    public static Coordinate minCoordinate(final Envelope envelope) {
        return new Coordinate(envelope.getMinX(), envelope.getMinY());
    }

    public static Coordinate maxCoordinate(final Envelope envelope) {
        return new Coordinate(envelope.getMaxX(), envelope.getMaxY());
    }

    public static Geometry toGeometry(final ReferencedEnvelope envelope) {
        final var crs = envelope.getCoordinateReferenceSystem();
        return toGeometry(envelope, CoordinateSystems.forCrs(crs));
    }

    public static Geometry toGeometry(
            final Envelope envelope, final CoordinateSystem<?> coordinateSystem) {
        return JTS.toGeometry(envelope, coordinateSystem.geometryFactory());
    }

    /**
     * Returns a coordinate in the interior of the geometry that represents the given geometry as
     * reasonably as possible.
     */
    public static Coordinate representativeCoordinate(final Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            throw new NoSuchElementException("No/empty geometry provided.");
        }
        return switch (geometry) {
            case final Point point -> point.getCoordinate();
            case final LineString lineString -> midPoint(lineString);
            case final Polygon polygon ->
                    maximumInscribedCircle(polygon).getCenter().getCoordinate();
            case final GeometryCollection geometryCollection ->
                    representativeCoordinate(geometryCollection);
            default -> geometry.getInteriorPoint().getCoordinate();
        };
    }

    private static Coordinate representativeCoordinate(
            final GeometryCollection geometryCollection) {
        final var centroid = geometryCollection.getCentroid();
        final var centroidCoordinate = centroid.getCoordinate();

        // Shortcut if centroid already in multi polygon
        if (geometryCollection instanceof MultiPolygon && geometryCollection.contains(centroid)) {
            return centroidCoordinate;
        }

        // Closest projection of centroid to multiline
        if (geometryCollection instanceof final MultiLineString multiLineString) {
            return getLinesStream(multiLineString)
                    .flatMap(JtsUtil::segmentsStream)
                    .min(comparing(segment -> segment.distance(centroidCoordinate)))
                    .map(segment -> projectCoordinateToSegment(segment, centroidCoordinate))
                    .orElseThrow();
        }

        // Calculate representative coordinates of each child geometry and use the one closest to
        // the centroid of the geometry collection
        return flatGeometriesStream(geometryCollection)
                .filter(Predicate.not(Geometry::isEmpty))
                .map(JtsUtil::representativeCoordinate)
                .min(comparing(c -> c.distance(centroidCoordinate)))
                .orElseThrow();
    }

    /** Projects the coordinate to the interior of the given segment. */
    public static Coordinate projectCoordinateToSegment(
            final LineSegment segment, final Coordinate coordinate) {
        final var t = Math.clamp(segment.projectionFactor(coordinate), 0, 1);
        final var p0 = segment.p0;
        final var p1 = segment.p1;
        final var coord = new Coordinate();
        coord.x = p0.x + t * (p1.x - p0.x);
        coord.y = p0.y + t * (p1.y - p0.y);
        if (Double.isFinite(p0.getZ()) || Double.isFinite(p1.getZ())) {
            coord.setZ(p0.getZ() + t * (p1.getZ() - p0.getZ()));
        }
        return coord;
    }

    public static MaximumInscribedCircle maximumInscribedCircle(final Polygon polygon) {
        final var tolerance = polygon.getEnvelopeInternal().getDiameter() / 100;
        return maximumInscribedCircle(polygon, tolerance);
    }

    public static MaximumInscribedCircle maximumInscribedCircle(
            final Polygon polygon, final double tolerance) {
        if (polygon == null || polygon.isEmpty()) {
            throw new NoSuchElementException("No/empty polygon provided.");
        }
        return new MaximumInscribedCircle(polygon, tolerance);
    }
}
