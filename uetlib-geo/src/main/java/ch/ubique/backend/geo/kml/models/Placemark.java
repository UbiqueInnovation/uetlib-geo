package ch.ubique.backend.geo.kml.models;

import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

public class Placemark {

    private String name;

    private String description;

    private String styleId;

    private Geometry geometry;

    private Map<String, String> extendedData;

    public Placemark() {
        extendedData = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<String, String> getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(final Map<String, String> extendedData) {
        this.extendedData = extendedData;
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(final String styleId) {
        this.styleId = styleId;
    }
}
