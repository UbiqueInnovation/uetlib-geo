package ch.ubique.backend.geo.coordinate.util.distance;

import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.Coordinate;

import org.springframework.core.GenericTypeResolver;

public interface DistanceCalculator<C extends Coordinate> {

    double getDistance(C c1, C c2);

    default double getDistance(
            final org.locationtech.jts.geom.Coordinate c1,
            final org.locationtech.jts.geom.Coordinate c2) {

        @SuppressWarnings("unchecked")
        final var coordinateClass =
                (Class<C>)
                        GenericTypeResolver.resolveTypeArgument(
                                getClass(), DistanceCalculator.class);

        final var coordinateSystem = CoordinateSystems.forClass(coordinateClass);
        return getDistance(coordinateSystem.fromJts(c1), coordinateSystem.fromJts(c2));
    }
}
