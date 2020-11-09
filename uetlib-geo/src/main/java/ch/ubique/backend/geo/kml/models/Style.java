package ch.ubique.backend.geo.kml.models;

public class Style {

    private ItemStyle iconStyle;
    private ItemStyle labelStyle;
    private ItemStyle lineStyle;

    public ItemStyle getIconStyle() {
        return iconStyle;
    }

    public Style setIconStyle(ItemStyle iconStyle) {
        this.iconStyle = iconStyle;
        return this;
    }

    public ItemStyle getLabelStyle() {
        return labelStyle;
    }

    public Style setLabelStyle(ItemStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public ItemStyle getLineStyle() {
        return lineStyle;
    }

    public Style setLineStyle(ItemStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    @Override
    public String toString() {
        return "Style{" +
                "iconStyle=" + iconStyle +
                ", labelStyle=" + labelStyle +
                ", lineStyle=" + lineStyle +
                '}';
    }
}
