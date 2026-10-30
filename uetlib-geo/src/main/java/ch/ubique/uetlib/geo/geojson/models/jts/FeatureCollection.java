package ch.ubique.uetlib.geo.geojson.models.jts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection
        extends GenericFeatureCollection<Feature, Map<String, Object>, FeatureCollection> {}
