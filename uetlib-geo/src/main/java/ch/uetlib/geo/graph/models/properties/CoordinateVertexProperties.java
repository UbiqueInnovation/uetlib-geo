package ch.uetlib.geo.graph.models.properties;

import ch.uetlib.geo.coordinate.models.Coordinate;

public interface CoordinateVertexProperties<C extends Coordinate> {

    C coordinate();
}
