package ch.ubique.backend.geo.graph.routing.utils;

import org.locationtech.jts.geom.Coordinate;

public record VertexPosition(int x, int y) {

    public VertexPosition(Coordinate coordinate) {
        this((int) coordinate.x, (int) coordinate.y);
    }
}
