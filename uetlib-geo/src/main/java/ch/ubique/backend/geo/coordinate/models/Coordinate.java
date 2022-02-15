package ch.ubique.backend.geo.coordinate.models;

import org.locationtech.jts.geom.Point;

public interface Coordinate<C extends Coordinate<C>> {

    Point toJtsPoint();

    double distanceTo(C other);

    double flatDistanceTo(C other);
}
