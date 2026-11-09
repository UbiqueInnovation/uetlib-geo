package ch.uetlib.geo.coordinate.util.distance;

import ch.uetlib.geo.coordinate.constants.CoordinateSystem;
import ch.uetlib.geo.coordinate.models.Coordinate;

import java.util.Comparator;

public interface DistanceCalculator<C extends Coordinate> {

    double getDistance(C c1, C c2);

    CoordinateSystem<C> coordinateSystem();

    default double getDistance(
            final org.locationtech.jts.geom.Coordinate c1,
            final org.locationtech.jts.geom.Coordinate c2) {

        return getDistance(coordinateSystem().fromJts(c1), coordinateSystem().fromJts(c2));
    }

    /** A comparator that compares coordinates to the given reference coordinate */
    default Comparator<C> compareByDistanceToCoordinate(final C reference) {
        return Comparator.comparingDouble(c -> getDistance(c, reference));
    }
}
