package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

public record TlmVertexPosition(int east, int north) {

    public TlmVertexPosition(LV95Coordinate coordinate) {
        this((int) coordinate.getEast(), (int) coordinate.getNorth());
    }
}
