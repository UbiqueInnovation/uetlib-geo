package ch.ubique.backend.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection extends GenericFeatureCollection<Feature> {}
