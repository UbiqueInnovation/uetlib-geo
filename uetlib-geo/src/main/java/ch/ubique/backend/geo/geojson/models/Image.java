package ch.ubique.backend.geo.geojson.models;

public class Image {

    private String color;

    private String codeIcon;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        if (!color.startsWith("#")) {
            this.color = "#" + this.color;
        }
    }

    public String getCodeIcon() {
        return codeIcon;
    }

    public void setCodeIcon(String codeIcon) {
        this.codeIcon = codeIcon;
    }
}
