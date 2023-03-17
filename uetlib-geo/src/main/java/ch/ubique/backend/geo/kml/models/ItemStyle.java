package ch.ubique.backend.geo.kml.models;

public class ItemStyle {

    private String color;
    private Double scale;
    private Double width;
    private Icon icon;

    public String getColor() {
        return color;
    }

    public ItemStyle setColor(final String color) {
        this.color = color;
        return this;
    }

    public Double getScale() {
        return scale;
    }

    public ItemStyle setScale(final Double scale) {
        this.scale = scale;
        return this;
    }

    public Double getWidth() {
        return width;
    }

    public ItemStyle setWidth(final Double width) {
        this.width = width;
        return this;
    }

    public Icon getIcon() {
        return icon;
    }

    public ItemStyle setIcon(final Icon icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public String toString() {
        return "ItemStyle{"
                + "color='"
                + color
                + '\''
                + ", scale="
                + scale
                + ", tileWidth="
                + width
                + ", icon="
                + icon
                + '}';
    }
}
