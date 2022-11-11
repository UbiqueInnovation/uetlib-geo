package ch.ubique.backend.geo.coordinate.util;

import org.locationtech.jts.geom.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public class JtsUtil {

    private JtsUtil() {}

    @NotNull
    public static MultiPoint toJtsMultiPoint(final Collection<Point> points) {
        return factoryFromGeometryCollection(points).createMultiPoint(points.toArray(Point[]::new));
    }

    @NotNull
    public static MultiLineString toJtsMultiLineString(Collection<LineString> lineStrings) {
        return factoryFromGeometryCollection(lineStrings).createMultiLineString(lineStrings.toArray(LineString[]::new));
    }

    @NotNull
    public static MultiPolygon toJtsMultiPolygon(Collection<Polygon> polygons) {
        return factoryFromGeometryCollection(polygons).createMultiPolygon(polygons.toArray(Polygon[]::new));
    }

    private static GeometryFactory factoryFromGeometryCollection(Collection<? extends Geometry> geometries) {
        final var geometryMaybe = geometries.stream().findAny();
        return geometryMaybe.map(Geometry::getFactory).orElse(new GeometryFactory());
    }
}
