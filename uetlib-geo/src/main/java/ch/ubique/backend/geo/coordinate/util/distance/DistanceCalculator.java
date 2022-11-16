package ch.ubique.backend.geo.coordinate.util.distance;

import org.locationtech.jts.geom.Coordinate;

public interface DistanceCalculator {
    double getDistance(Coordinate c1, Coordinate c2);
}
