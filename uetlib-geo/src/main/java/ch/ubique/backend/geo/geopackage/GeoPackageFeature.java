package ch.ubique.backend.geo.geopackage;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public record GeoPackageFeature(
        Geometry geometry,
        Map<String, Object> attributes
) {}
