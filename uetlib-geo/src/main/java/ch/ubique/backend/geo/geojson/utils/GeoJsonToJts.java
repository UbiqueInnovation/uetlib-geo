package ch.ubique.backend.geo.geojson.utils;

import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import ch.ubique.backend.geo.geojson.models.geometries.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GeoJsonToJts {

    private final GeometryFactory geometryFactory;

    private final Function<Coordinate, org.locationtech.jts.geom.Coordinate> coordinateMapper;

    private GeoJsonToJts(int srid, Function<Coordinate, org.locationtech.jts.geom.Coordinate> coordinateMapper) {
        geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
        this.coordinateMapper = coordinateMapper;
    }

    public static GeoJsonToJts lv03() {
        return new GeoJsonToJts(21781, c -> JtsConverter.toJts(c.asLv03Coordinate()));
    }

    public static GeoJsonToJts lv95() {
        return new GeoJsonToJts(2056, c -> JtsConverter.toJts(c.asLv95Coordinate()));
    }

    public static GeoJsonToJts wgs84() {
        return new GeoJsonToJts(4326, c -> JtsConverter.toJts(c.asWgs84Coordinate()));
    }

    public static GeoJsonToJts webMercator() {
        return new GeoJsonToJts(3857, c -> JtsConverter.toJts(c.asWebMercatorCoordinate()));
    }

    public org.locationtech.jts.geom.Geometry convert(Geometry geometry) {
        if (geometry instanceof Point point) {
            return convert(point);
        }
        if (geometry instanceof LineString lineString) {
            return convert(lineString);
        }
        if (geometry instanceof Polygon polygon) {
            return convert(polygon);
        }
        if (geometry instanceof MultiPoint multiPoint) {
            return convert(multiPoint);
        }
        if (geometry instanceof MultiLineString multiLineString) {
            return convert(multiLineString);
        }
        if (geometry instanceof MultiPolygon multiPolygon) {
            return convert(multiPolygon);
        }
        throw new UnsupportedOperationException("Unsupported geometry " + geometry);
    }

    public org.locationtech.jts.geom.Point convert(Point point) {
        return geometryFactory.createPoint(coordinateMapper.apply(point.getCoordinate()));
    }

    public org.locationtech.jts.geom.LineString convert(LineString lineString) {
        return geometryFactory.createLineString(convert(lineString.getCoordinates()));
    }

    public org.locationtech.jts.geom.Polygon convert(Polygon polygon) {
        return geometryFactory.createPolygon(
                asLinearRing(polygon.getExteriorRing()),
                asLinearRings(polygon.getInteriorRings()));
    }

    public org.locationtech.jts.geom.MultiPoint convert(MultiPoint multiPoint) {
        return geometryFactory.createMultiPoint(multiPoint.getPoints().stream()
                .map(this::convert)
                .toArray(org.locationtech.jts.geom.Point[]::new));
    }

    public org.locationtech.jts.geom.MultiLineString convert(MultiLineString multiLineString) {
        return geometryFactory.createMultiLineString(multiLineString.getLineStrings().stream()
                .map(this::convert)
                .toArray(org.locationtech.jts.geom.LineString[]::new));
    }

    public org.locationtech.jts.geom.MultiPolygon convert(MultiPolygon multiPolygon) {
        return geometryFactory.createMultiPolygon(multiPolygon.getPolygons().stream()
                .map(this::convert)
                .toArray(org.locationtech.jts.geom.Polygon[]::new));
    }

    public org.locationtech.jts.geom.Coordinate[] convert(Collection<Coordinate> coordinates) {
        return coordinates.stream().map(coordinateMapper).toArray(org.locationtech.jts.geom.Coordinate[]::new);
    }

    public LinearRing asLinearRing(Collection<Coordinate> coordinates) {
        return geometryFactory.createLinearRing(convert(coordinates));
    }

    public LinearRing[] asLinearRings(Collection<List<Coordinate>> coordinates) {
        return coordinates.stream().map(this::asLinearRing).toArray(LinearRing[]::new);
    }
}
