package ch.uetlib.geo.osm.parser.graph;

import ch.uetlib.geo.coordinate.models.WGS84Coordinate;
import ch.uetlib.geo.graph.models.properties.CoordinateVertexProperties;

import java.util.Map;

public record OsmVertexProperties(WGS84Coordinate coordinate, Map<String, String> tags)
        implements CoordinateVertexProperties<WGS84Coordinate> {}
