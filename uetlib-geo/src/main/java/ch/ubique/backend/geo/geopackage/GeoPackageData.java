package ch.ubique.backend.geo.geopackage;

import java.util.Collection;
import java.util.Map;

public record GeoPackageData(
        Collection<GeoPackageFeature> features,
        Map<String, Collection<Map<String, Object>>> attributes) {}
