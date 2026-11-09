package ch.uetlib.geo.tlm.models;

import ch.uetlib.geo.coordinate.models.LV95Coordinate;
import ch.uetlib.geo.graph.models.properties.CoordinateVertexProperties;

public record TlmVertexProperties(LV95Coordinate coordinate)
        implements CoordinateVertexProperties<LV95Coordinate> {}
