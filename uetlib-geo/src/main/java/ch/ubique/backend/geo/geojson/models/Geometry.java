package ch.ubique.backend.geo.geojson.models;

import java.util.List;

public class Geometry {

    private List<Object> coordinates;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Object> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Object> coordinates) {
        this.coordinates = coordinates;
    }
}
