package ch.ubique.backend.geo.coordinate.util.distance;

import org.locationtech.jts.geom.Coordinate;

public class SwissDistanceCalculator implements DistanceCalculator {

    @Override
    public double getDistance(Coordinate c1, Coordinate c2) {
        return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
    }
}
