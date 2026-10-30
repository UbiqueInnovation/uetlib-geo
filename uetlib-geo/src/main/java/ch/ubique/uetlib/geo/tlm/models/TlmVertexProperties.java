package ch.ubique.uetlib.geo.tlm.models;

import ch.ubique.uetlib.geo.coordinate.models.LV95Coordinate;
import ch.ubique.uetlib.geo.graph.models.properties.CoordinateVertexProperties;

public record TlmVertexProperties(LV95Coordinate coordinate)
        implements CoordinateVertexProperties<LV95Coordinate> {}
