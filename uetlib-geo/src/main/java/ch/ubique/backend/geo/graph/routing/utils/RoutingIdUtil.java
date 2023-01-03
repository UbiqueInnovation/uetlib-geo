package ch.ubique.backend.geo.graph.routing.utils;

import static org.locationtech.jts.geom.Coordinate.NULL_ORDINATE;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;

public class RoutingIdUtil {

    private RoutingIdUtil() {}

    /**
     * Identifier for routing vector tiles has to be computed in this way so we can find out in
     * which tile the neighbor lies
     */
    public static String getRoutingIdentifier(Coordinate coordinate) {
        final var east = paddedString(coordinate.getX(), 10);
        final var north = paddedString(coordinate.getY(), 10);
        final var elevation =
                paddedString(coordinate.getZ() == NULL_ORDINATE ? 0 : coordinate.getZ(), 6);
        return east + north + elevation;
    }

    private static String paddedString(final double value, final int size) {
        return StringUtils.leftPad(String.valueOf(Math.round(value * 100)), size, "0");
    }
}
