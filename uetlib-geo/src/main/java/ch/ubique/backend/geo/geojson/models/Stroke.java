package ch.ubique.backend.geo.geojson.models;

public class Stroke {

    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        if (!color.startsWith("#")) {
            this.color = "#" + this.color;
        }
    }
}
