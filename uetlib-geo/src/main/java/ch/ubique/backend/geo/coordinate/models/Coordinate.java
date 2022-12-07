package ch.ubique.backend.geo.coordinate.models;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface Coordinate {

    int srid();

    CoordinateReferenceSystem crs();

    org.locationtech.jts.geom.Coordinate toJts();

    org.locationtech.jts.geom.Point toJtsPoint();

    double distanceTo(Coordinate other);

    double flatDistanceTo(Coordinate other);
}
