package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.geometry.models.Point;

import java.util.List;
import java.util.stream.Collectors;

public class PointConverter {

    private PointConverter() {}

    public static <P extends Point<P>> List<P> lv95toPoints(
            Class<P> cls, List<LV95Coordinate> coordinates) {
        return coordinates.stream().map(c -> toPoint(cls, c)).collect(Collectors.toList());
    }

    public static <P extends Point<P>> P toPoint(Class<P> cls, Coordinate coordinate) {
        return toPoint(cls, coordinate.toJts());
    }

    public static <P extends Point<P>> P toPoint(
            Class<P> cls, org.locationtech.jts.geom.Coordinate coordinate) {
        if (Double.isNaN(coordinate.getZ())) {
            return Point.newInstance(cls, coordinate.getX(), coordinate.getY());
        }
        return Point.newInstance(cls, coordinate.getX(), coordinate.getY(), coordinate.getZ());
    }

    public static <P extends Point<P>> List<LV95Coordinate> toLv95Coordinate(List<P> points) {
        return points.stream().map(PointConverter::toLv95Coordinate).collect(Collectors.toList());
    }

    public static <P extends Point<P>> LV95Coordinate toLv95Coordinate(P point) {
        if (point.getDimension() == 2) {
            return new LV95Coordinate(point.get(0), point.get(1));
        }
        return new LV95Coordinate(point.get(0), point.get(1), point.get(2));
    }

    public static <P extends Point<P>> List<P> lv03toPoints(
            Class<P> cls, List<LV03Coordinate> coordinates) {
        return coordinates.stream().map(c -> toPoint(cls, c)).collect(Collectors.toList());
    }

    public static <P extends Point<P>> P toPoint(Class<P> cls, LV03Coordinate coordinate) {
        return coordinate
                .getOptionalElevation()
                .map(
                        elevation ->
                                Point.newInstance(
                                        cls,
                                        coordinate.getEast(),
                                        coordinate.getNorth(),
                                        elevation))
                .orElseGet(
                        () -> Point.newInstance(cls, coordinate.getEast(), coordinate.getNorth()));
    }

    public static <P extends Point<P>> List<LV03Coordinate> toLv03Coordinate(List<P> points) {
        return points.stream().map(PointConverter::toLv03Coordinate).collect(Collectors.toList());
    }

    public static <P extends Point<P>> LV03Coordinate toLv03Coordinate(P point) {
        if (point.getDimension() == 2) {
            return new LV03Coordinate(point.get(0), point.get(1));
        }
        return new LV03Coordinate(point.get(0), point.get(1), point.get(2));
    }

    public static <P extends Point<P>> List<P> wgs84toPoints(
            Class<P> cls, List<WGS84Coordinate> coordinates) {
        return coordinates.stream().map(c -> toPoint(cls, c)).collect(Collectors.toList());
    }

    public static <P extends Point<P>> P toPoint(Class<P> cls, WGS84Coordinate coordinate) {
        return coordinate
                .getOptionalElevation()
                .map(
                        elevation ->
                                Point.newInstance(
                                        cls,
                                        coordinate.getLatitude(),
                                        coordinate.getLongitude(),
                                        elevation))
                .orElseGet(
                        () ->
                                Point.newInstance(
                                        cls, coordinate.getLatitude(), coordinate.getLongitude()));
    }

    public static <P extends Point<P>> List<WGS84Coordinate> toWgs84Coordinate(List<P> points) {
        return points.stream().map(PointConverter::toWgs84Coordinate).collect(Collectors.toList());
    }

    public static <P extends Point<P>> WGS84Coordinate toWgs84Coordinate(P point) {
        if (point.getDimension() == 2) {
            return new WGS84Coordinate(point.get(0), point.get(1));
        }
        return new WGS84Coordinate(point.get(0), point.get(1), point.get(2));
    }
}
