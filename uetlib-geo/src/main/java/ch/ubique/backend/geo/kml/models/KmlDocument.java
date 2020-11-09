package ch.ubique.backend.geo.kml.models;

import java.util.Collection;
import java.util.Map;

public class KmlDocument {

    private Collection<Placemark> placemarks;
    private Map<String, StyleMap> styleMaps;
    private Map<String, Style> styles;

    public Collection<Placemark> getPlacemarks() {
        return placemarks;
    }

    public KmlDocument setPlacemarks(Collection<Placemark> placemarks) {
        this.placemarks = placemarks;
        return this;
    }

    public Map<String, StyleMap> getStyleMaps() {
        return styleMaps;
    }

    public KmlDocument setStyleMaps(Map<String, StyleMap> styleMaps) {
        this.styleMaps = styleMaps;
        return this;
    }

    public Map<String, Style> getStyles() {
        return styles;
    }

    public KmlDocument setStyles(Map<String, Style> styles) {
        this.styles = styles;
        return this;
    }
}
