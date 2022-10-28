package ch.ubique.backend.geo.geojson.utils;

import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.geometries.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Collection;
import java.util.List;

public class GeoJsonToJts {

    private final GeometryFactory geometryFactory;

    private GeoJsonToJts(int srid) {
        geometryFactory = new GeometryFactory(new PrecisionModel(), srid);
    }

    public static GeoJsonToJts wgs84() {
        return new GeoJsonToJts(4326);
    }

    public org.locationtech.jts.geom.Point convert(Point point) {
        return geometryFactory.createPoint(point.getCoordinate().asJts());
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
        return coordinates.stream().map(Coordinate::asJts).toArray(org.locationtech.jts.geom.Coordinate[]::new);
    }

    public LinearRing asLinearRing(Collection<Coordinate> coordinates) {
        return geometryFactory.createLinearRing(convert(coordinates));
    }

    public LinearRing[] asLinearRings(Collection<List<Coordinate>> coordinates) {
        return coordinates.stream().map(this::asLinearRing).toArray(LinearRing[]::new);
    }
}
