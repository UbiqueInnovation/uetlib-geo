package ch.ubique.backend.geo.coordinate.models;

public interface Coordinate {

    org.locationtech.jts.geom.Coordinate toJts();

    org.locationtech.jts.geom.Point toJtsPoint();

    double distanceTo(Coordinate other);

    double flatDistanceTo(Coordinate other);
}
