package ch.ubique.backend.geo.geojson.models;

public class Style {

    private Stroke stroke;

    private Image image;

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
