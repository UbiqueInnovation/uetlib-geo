package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.validation.constraints.NotNull;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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

    private static GeometryFactory factoryFromGeometryCollection(
            Collection<? extends Geometry> geometries) {
        final var geometryMaybe = geometries.stream().findAny();
        return geometryMaybe.map(Geometry::getFactory).orElse(new GeometryFactory());
    }

    public static CoordinateReferenceSystem getCrs(Geometry geometry) throws FactoryException {
        return getCrs(geometry.getSRID());
    }

    public static CoordinateReferenceSystem getCrs(int srid) throws FactoryException {
        if (srid == 0) {
            throw new MissingInfoException(
                    "The Spatial Reference ID (SRID) is not set for the given geometry.");
        }
        if (srid == 4326) {
            // the default for EPSG:4326 is latitude first. our JtsConverter as well as PostGIS
            // works with longitude first, so this will be our default
            return CRS.decode("EPSG:4326", true);
        }
        return CRS.decode("EPSG:" + srid);
    }

    public static List<Geometry> getSimpleGeometries(Geometry geometry) {
        if (geometry.getNumGeometries() == 1) {
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

    public static Geometry tryToGetValidGeometry(Geometry geometry)
            throws UnfixableGeometryException {
        final var validator = new IsValidOp(geometry);
        if (!validator.isValid()) {
            logger.info("Attempting to fix invalid geometry ({})", validator.getValidationError());
            geometry = GeometryFixer.fix(geometry);

            final var fixedValidator = new IsValidOp(geometry);
            if (!fixedValidator.isValid()) {
                throw new UnfixableGeometryException(fixedValidator.getValidationError());
            }
        }
        return geometry;
    }
}
