package ch.ubique.backend.geo.coordinate.util.distance.impl;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.LV03Coordinate;
import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculator;

public class LV95DistanceCalculator implements DistanceCalculator<LV95Coordinate> {

    @Override
    public double getDistance(final LV95Coordinate c1, final LV95Coordinate c2) {
        return Math.sqrt(Math.pow(c1.east() - c2.east(), 2) + Math.pow(c1.north() - c2.north(), 2));
    }

    @Override
    public CoordinateSystem<LV95Coordinate> coordinateSystem() {
        return CoordinateSystems.LV95;
    }
}
