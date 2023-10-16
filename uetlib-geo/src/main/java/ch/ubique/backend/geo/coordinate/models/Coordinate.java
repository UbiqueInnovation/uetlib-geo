package ch.ubique.backend.geo.coordinate.models;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

public sealed interface Coordinate
        permits LV03Coordinate, LV95Coordinate, WebMercatorCoordinate, WGS84Coordinate {

    int srid();

    CoordinateReferenceSystem crs();

    org.locationtech.jts.geom.Coordinate toJts();

    org.locationtech.jts.geom.Point toJtsPoint();

    double distanceTo(Coordinate other);

    double flatDistanceTo(Coordinate other);
}
