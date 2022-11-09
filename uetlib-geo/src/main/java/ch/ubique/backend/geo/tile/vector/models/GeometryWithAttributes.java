package ch.ubique.backend.geo.tile.vector.models;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public record GeometryWithAttributes(Geometry geometry, Map<String, Object> attributes) {

}
