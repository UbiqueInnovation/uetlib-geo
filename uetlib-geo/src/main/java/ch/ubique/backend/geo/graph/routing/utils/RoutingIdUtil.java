package ch.ubique.backend.geo.graph.routing.utils;

import org.locationtech.jts.geom.Coordinate;

public class RoutingIdUtil {

    private RoutingIdUtil() {
    }

    /**
     * Identifier for routing vector tiles has to be computed in this way
     * so we can find out in which tile the neighbor lies
     */
    public static String getRoutingIdentifier(Coordinate coordinate) {
        final var east = String.format("%010d", Math.round(coordinate.getX() * 100));
        final var north = String.format("%010d", Math.round(coordinate.getY() * 100));
        final var elevation = String.format("%06d", Math.round(coordinate.getZ() == Coordinate.NULL_ORDINATE ? 0 : coordinate.getZ() * 100));
        return east + north + elevation;
    }

}
