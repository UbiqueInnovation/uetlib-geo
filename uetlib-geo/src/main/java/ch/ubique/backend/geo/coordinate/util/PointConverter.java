package ch.ubique.backend.geo.coordinate.util;

import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.geometry.models.Point;
import ch.ubique.backend.geo.geometry.models.Point2d;
import ch.ubique.backend.geo.geometry.models.Point3d;

import java.util.List;
import java.util.stream.Collectors;

public class PointConverter {

    public static List<Point<?>> lv95toPoints(List<LV95Coordinate> coordinates) {
        return coordinates.stream().map(PointConverter::toPoint).collect(Collectors.toList());
    }

    public static Point<?> toPoint(LV95Coordinate coordinate) {
        if (coordinate.getElevation() != null) {
            return new Point3d(coordinate.getEast(), coordinate.getNorth(), coordinate.getElevation());
        }
        return new Point2d(coordinate.getEast(), coordinate.getNorth());
    }

    public static List<LV95Coordinate> toLv95Coordinate(List<Point<?>> points) {
        return points.stream().map(PointConverter::toLv95Coordinate).collect(Collectors.toList());
    }

    public static LV95Coordinate toLv95Coordinate(Point<?> point) {
        if (point.getDimension() == 2) {
            return new LV95Coordinate(point.get(0), point.get(1));
        }
        return new LV95Coordinate(point.get(0), point.get(1), point.get(2));
    }


    public static List<Point<?>> lv03toPoints(List<LV03Coordinate> coordinates) {
        return coordinates.stream().map(PointConverter::toPoint).collect(Collectors.toList());
    }

    public static Point<?> toPoint(LV03Coordinate coordinate) {
        if (coordinate.getElevation() != null) {
            return new Point3d(coordinate.getEast(), coordinate.getNorth(), coordinate.getElevation());
        }
        return new Point2d(coordinate.getEast(), coordinate.getNorth());
    }

    public static List<LV03Coordinate> toLv03Coordinate(List<Point<?>> points) {
        return points.stream().map(PointConverter::toLv03Coordinate).collect(Collectors.toList());
    }

    public static LV03Coordinate toLv03Coordinate(Point<?> point) {
        if (point.getDimension() == 2) {
            return new LV03Coordinate(point.get(0), point.get(1));
        }
        return new LV03Coordinate(point.get(0), point.get(1), point.get(2));
    }


    public static List<Point<?>> wgs84toPoints(List<WGS84Coordinate> coordinates) {
        return coordinates.stream().map(PointConverter::toPoint).collect(Collectors.toList());
    }

    public static Point<?> toPoint(WGS84Coordinate coordinate) {
        if (coordinate.getElevation() != null) {
            return new Point3d(coordinate.getLatitude(), coordinate.getLongitude(), coordinate.getElevation());
        }
        return new Point2d(coordinate.getLatitude(), coordinate.getLongitude());
    }

    public static List<WGS84Coordinate> toWgs84Coordinate(List<Point<?>> points) {
        return points.stream().map(PointConverter::toWgs84Coordinate).collect(Collectors.toList());
    }

    public static WGS84Coordinate toWgs84Coordinate(Point<?> point) {
        if (point.getDimension() == 2) {
            return new WGS84Coordinate(point.get(0), point.get(1));
        }
        return new WGS84Coordinate(point.get(0), point.get(1), point.get(2));
    }
}
