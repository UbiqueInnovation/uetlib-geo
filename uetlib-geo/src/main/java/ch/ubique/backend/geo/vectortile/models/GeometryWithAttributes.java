package ch.ubique.backend.geo.vectortile.models;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public class GeometryWithAttributes {

    private final Geometry geometry;
    private final Map<String, Object> attributes;

    public GeometryWithAttributes(Geometry geometry, Map<String, Object> attributes) {
        this.geometry = geometry;
        this.attributes = attributes;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
