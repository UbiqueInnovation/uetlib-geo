package ch.ubique.backend.geo.coordinate.util;

import static org.locationtech.jts.noding.SegmentStringUtil.extractSegmentStrings;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;

import jakarta.validation.constraints.NotNull;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.cs.AxisDirection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JtsUtil {
    private static final Logger logger = LoggerFactory.getLogger(JtsUtil.class);

    private JtsUtil() {}

    @NotNull
    public static MultiPoint toJtsMultiPoint(@NotNull final Collection<Point> points) {
        return factoryFromGeometryCollection(points).createMultiPoint(points.toArray(Point[]::new));
    }

    @NotNull
    public static MultiPoint toJtsMultiPoint(@NotNull final Stream<Point> points) {
        final var array = points.toArray(Point[]::new);
        return factoryFromGeometryCollection(array).createMultiPoint(array);
    }

    @NotNull
    public static MultiLineString toJtsMultiLineString(
            @NotNull final Collection<LineString> lineStrings) {
        return factoryFromGeometryCollection(lineStrings)
                .createMultiLineString(lineStrings.toArray(LineString[]::new));
    }

    @NotNull
    public static MultiLineString toJtsMultiLineString(
            @NotNull final Stream<LineString> lineStrings) {
        final var array = lineStrings.toArray(LineString[]::new);
        return factoryFromGeometryCollection(array).createMultiLineString(array);
    }

    @NotNull
    public static MultiPolygon toJtsMultiPolygon(@NotNull final Collection<Polygon> polygons) {
        return factoryFromGeometryCollection(polygons)
                .createMultiPolygon(polygons.toArray(Polygon[]::new));
    }

    @NotNull
    public static MultiPolygon toJtsMultiPolygon(@NotNull final Stream<Polygon> polygons) {
        final var array = polygons.toArray(Polygon[]::new);
        return factoryFromGeometryCollection(array).createMultiPolygon(array);
    }

    @NotNull
    public static GeometryCollection toGeometryCollection(
            @NotNull final Collection<Geometry> geometries) {
        return factoryFromGeometryCollection(geometries)
                .createGeometryCollection(flattenGeometries(geometries).toArray(Geometry[]::new));
    }

    @NotNull
    public static GeometryCollection toGeometryCollection(
            @NotNull final Stream<Geometry> geometries) {
        final var array = flattenGeometries(geometries).toArray(Geometry[]::new);
        return factoryFromGeometryCollection(array).createGeometryCollection(array);
    }

    public static GeometryFactory factoryFromGeometryCollection(
            @NotNull final Collection<? extends Geometry> geometries) {
        final var geometryMaybe = geometries.stream().findAny();
        return geometryMaybe.map(Geometry::getFactory).orElse(new GeometryFactory());
    }

    public static GeometryFactory factoryFromGeometryCollection(
            @NotNull final Geometry[] geometries) {
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
        final var coordinates = lineString.getCoordinates();
        return IntStream.range(0, coordinates.length - 1)
                .mapToObj(i -> new LineSegment(coordinates[i], coordinates[i + 1]))
                .toList();
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
    @NotNull
    public static Stream<Geometry> getGeometriesAsStream(final Geometry geometry) {
        return IntStream.range(0, geometry.getNumGeometries()).mapToObj(geometry::getGeometryN);
    }

    @NotNull
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

    @NotNull
    public static Geometry tryToGetValidGeometry(@NotNull final Geometry geometry)
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
        geometry.setSRID(srid);
        getFlatGeometriesIterator(geometry).forEachRemaining(g -> g.setSRID(srid));
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
            return flatGeometriesStream(geometry).findAny().orElseThrow();
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
}
