package ch.uetlib.geo.coordinate.util.distance.impl;

import ch.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.coordinate.models.LV03Coordinate;
import ch.uetlib.geo.coordinate.util.distance.DistanceCalculator;

public class LV03DistanceCalculator implements DistanceCalculator<LV03Coordinate> {

    @Override
    public double getDistance(final LV03Coordinate c1, final LV03Coordinate c2) {
        return Math.sqrt(Math.pow(c1.east() - c2.east(), 2) + Math.pow(c1.north() - c2.north(), 2));
    }

    @Override
    public CoordinateSystem<LV03Coordinate> coordinateSystem() {
        return CoordinateSystems.LV03;
    }
}
