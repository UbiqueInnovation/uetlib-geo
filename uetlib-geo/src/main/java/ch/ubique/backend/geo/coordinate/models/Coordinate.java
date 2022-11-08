package ch.ubique.backend.geo.coordinate.models;

import org.locationtech.jts.geom.Point;

public interface Coordinate {

    Point toJtsPoint();

    double distanceTo(Coordinate other);

    double flatDistanceTo(Coordinate other);
}
