package ch.ubique.backend.geo.tile.vector.models;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

/**
 * @deprecated this was only used for the {@link TileElement}, which has been replaced.
 * Will be removed with version 0.9.0
 */
@Deprecated(since = "0.8.0", forRemoval = true)
public record GeometryWithAttributes(Geometry geometry, Map<String, Object> attributes) {

}
