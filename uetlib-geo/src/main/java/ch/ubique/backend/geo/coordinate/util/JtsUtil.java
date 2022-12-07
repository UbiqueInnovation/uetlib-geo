package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import org.eclipse.emf.common.util.WrappedException;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JtsUtil {
    private static final Logger logger = LoggerFactory.getLogger(JtsUtil.class);

    private JtsUtil() {}

    @NotNull
    public static MultiPoint toJtsMultiPoint(final Collection<Point> points) {
        return factoryFromGeometryCollection(points).createMultiPoint(points.toArray(Point[]::new));
    }

    @NotNull
    public static MultiLineString toJtsMultiLineString(Collection<LineString> lineStrings) {
        return factoryFromGeometryCollection(lineStrings)
                .createMultiLineString(lineStrings.toArray(LineString[]::new));
    }

    @NotNull
    public static MultiPolygon toJtsMultiPolygon(Collection<Polygon> polygons) {
        return factoryFromGeometryCollection(polygons)
                .createMultiPolygon(polygons.toArray(Polygon[]::new));
    }

    public static GeometryFactory factoryFromGeometryCollection(
            Collection<? extends Geometry> geometries) {
        final var geometryMaybe = geometries.stream().findAny();
        return geometryMaybe.map(Geometry::getFactory).orElse(new GeometryFactory());
    }

    public static CoordinateReferenceSystem getCrs(Geometry geometry) {
        return getCrs(geometry.getSRID());
    }

    public static CoordinateReferenceSystem getCrs(int srid) {
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
        } catch (FactoryException e) {
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

    public static List<Geometry> getSimpleGeometries(Geometry geometry) {
        if (!(geometry instanceof GeometryCollection)) {
            // Not a collection -> already simple -> return geometry itself
            return List.of(geometry);
        }

        // Get geometries of geometry collection and recursively
        // call this function until all geometries are simple
        return IntStream.range(0, geometry.getNumGeometries())
                .mapToObj(geometry::getGeometryN)
                .flatMap(g -> getSimpleGeometries(g).stream()) // recursive call
                .toList();
    }

    public static Iterator<Geometry> getSimpleGeometriesIterator(Geometry geometry) {
        return getSimpleGeometries(geometry).iterator();
    }

    public static Geometry tryToGetValidGeometry(final Geometry geometry)
            throws UnfixableGeometryException {
        final var validator = new IsValidOp(geometry);

        if (validator.isValid()) {
            return geometry;
        }

        logger.info("Attempting to fix invalid geometry ({})", validator.getValidationError());
        final var fixedGeometry = GeometryFixer.fix(geometry);

        final var fixedValidator = new IsValidOp(fixedGeometry);
        if (fixedValidator.isValid()) {
            return fixedGeometry;
        }

        throw new UnfixableGeometryException(fixedValidator.getValidationError());
    }
}
