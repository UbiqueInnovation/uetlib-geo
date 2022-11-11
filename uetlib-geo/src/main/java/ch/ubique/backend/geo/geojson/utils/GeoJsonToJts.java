package ch.ubique.backend.geo.geojson.utils;


import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.models.WebMercatorCoordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geojson.models.Coordinate;
import ch.ubique.backend.geo.geojson.models.Geometry;
import ch.ubique.backend.geo.geojson.models.geometries.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GeoJsonToJts<C extends ch.ubique.backend.geo.coordinate.models.Coordinate> {

    private final JtsConverter<C> jtsConverter;

    private final Function<Coordinate, C> mapToBackendLib;

    private GeoJsonToJts(JtsConverter<C> jtsConverter, Function<Coordinate, C> mapToBackendLib) {
        this.jtsConverter = jtsConverter;
        this.mapToBackendLib = mapToBackendLib;
    }

    public static final GeoJsonToJts<LV03Coordinate> LV03 =
            new GeoJsonToJts<>(JtsConverter.LV03, Coordinate::asLv03Coordinate);

    public static final GeoJsonToJts<LV95Coordinate> LV95 =
            new GeoJsonToJts<>(JtsConverter.LV95, Coordinate::asLv95Coordinate);

    public static final GeoJsonToJts<WGS84Coordinate> WGS84 =
            new GeoJsonToJts<>(JtsConverter.WGS84, Coordinate::asWgs84Coordinate);

    public static final GeoJsonToJts<WebMercatorCoordinate> WEB_MERCATOR =
            new GeoJsonToJts<>(JtsConverter.WEB_MERCATOR, Coordinate::asWebMercatorCoordinate);

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
        return jtsConverter.toJtsPoint(toBackendLib(point.getCoordinate()));
    }

    public org.locationtech.jts.geom.LineString convert(LineString lineString) {
        return jtsConverter.toJtsLineString(toBackendLib(lineString.getCoordinates()));
    }

    public org.locationtech.jts.geom.Polygon convert(Polygon polygon) {
        return jtsConverter.toJtsPolygon(
                toBackendLib(polygon.getExteriorRing()),
                polygon.getInteriorRings().stream().map(this::toBackendLib).toList());
    }

    public org.locationtech.jts.geom.MultiPoint convert(MultiPoint multiPoint) {
        return JtsUtil.toJtsMultiPoint(multiPoint.getPoints().stream().map(this::convert).toList());
    }

    public org.locationtech.jts.geom.MultiLineString convert(MultiLineString multiLineString) {
        return JtsUtil.toJtsMultiLineString(multiLineString.getLineStrings().stream().map(this::convert).toList());

    }

    public org.locationtech.jts.geom.MultiPolygon convert(MultiPolygon multiPolygon) {
        return JtsUtil.toJtsMultiPolygon(multiPolygon.getPolygons().stream().map(this::convert).toList());
    }

    private C toBackendLib(Coordinate coordinate) {
        return mapToBackendLib.apply(coordinate);
    }

    private List<C> toBackendLib(Collection<Coordinate> coordinates) {
        return coordinates.stream().map(mapToBackendLib).toList();
    }
}
