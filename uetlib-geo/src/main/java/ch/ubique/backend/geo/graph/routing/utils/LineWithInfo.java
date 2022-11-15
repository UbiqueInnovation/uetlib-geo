package ch.ubique.backend.geo.graph.routing.utils;

import org.locationtech.jts.geom.LineString;

public record LineWithInfo<T>(
        LineString line,
        T info
) {
}
