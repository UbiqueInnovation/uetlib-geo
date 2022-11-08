package ch.ubique.backend.geo.osm.parser.graph;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.graph.models.properties.CoordinateVertexProperties;

import java.util.Map;

public record OsmVertexProperties(Coordinate coordinate, Map<String, String> tags) implements CoordinateVertexProperties {
}
