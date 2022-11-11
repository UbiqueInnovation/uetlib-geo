package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;

public record TlmVertexProperties(LV95Coordinate coordinate) implements CoordinateVertexProperties<LV95Coordinate> {
}
