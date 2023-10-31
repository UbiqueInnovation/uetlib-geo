package ch.ubique.backend.geo.coordinate.util.distance.impl;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystem;
import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.coordinate.util.distance.DistanceCalculator;

import org.geotools.referencing.GeodeticCalculator;

public class GenericDistanceCalculator<C extends Coordinate> implements DistanceCalculator<C> {
    private final GeodeticCalculator geodeticCalculator;

    public GenericDistanceCalculator(final CoordinateSystem<C> coordinateSystem) {
        this.geodeticCalculator = new GeodeticCalculator(coordinateSystem.crs());
    }

    @Override
    public double getDistance(final C c1, final C c2) {
        final var start = JtsConverter.WGS84.transform(c1);
        final var end = JtsConverter.WGS84.transform(c2);
        geodeticCalculator.setStartingGeographicPoint(start.getLongitude(), start.getLatitude());
        geodeticCalculator.setDestinationGeographicPoint(end.getLongitude(), end.getLatitude());
        return geodeticCalculator.getOrthodromicDistance();
    }
}
