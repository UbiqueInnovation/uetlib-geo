package ch.ubique.backend.geo.coordinate.util.distance.impl;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculator;

public class LV95DistanceCalculator implements DistanceCalculator<LV95Coordinate> {

    @Override
    public double getDistance(final LV95Coordinate c1, final LV95Coordinate c2) {
        return Math.sqrt(
                Math.pow(c1.getEast() - c2.getEast(), 2)
                        + Math.pow(c1.getNorth() - c2.getNorth(), 2));
    }
}
