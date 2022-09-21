package ch.ubique.backend.geo.kml.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Placemark {

    private String name;

    private String description;

    private String styleId;

    // placemarks can have multiple geometries
    private List<List<LV95Coordinate>> coordinates = new ArrayList<>();

    private Map<String, String> extendedData;

    public Placemark() {
        extendedData = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<List<LV95Coordinate>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<LV95Coordinate>> coordinates) {
        this.coordinates = coordinates;
    }

    public void addCoordinates(List<LV95Coordinate> coordinates) {
        this.coordinates.add(coordinates);
    }

    public Map<String, String> getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(Map<String, String> extendedData) {
        this.extendedData = extendedData;
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }
}
