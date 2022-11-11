package ch.ubique.backend.geo.osm.parser.graph;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;

import java.util.Map;

public record OsmVertexProperties(WGS84Coordinate coordinate, Map<String, String> tags)
        implements CoordinateVertexProperties<WGS84Coordinate> {
}
