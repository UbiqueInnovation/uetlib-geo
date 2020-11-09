package ch.ubique.backend.geo.kml.models;

import java.util.HashMap;
import java.util.Map;

public class StyleMap {

    private Map<String, String> map;

    public StyleMap() {
        map = new HashMap<>();
    }

    public Map<String, String> getMap() {
        return map;
    }

    public StyleMap setMap(Map<String, String> map) {
        this.map = map;
        return this;
    }
}
