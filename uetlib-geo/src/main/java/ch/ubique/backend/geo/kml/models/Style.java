package ch.ubique.backend.geo.kml.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Style {

    private ItemStyle iconStyle;
    private ItemStyle labelStyle;
    private ItemStyle lineStyle;

    public ItemStyle getIconStyle() {
        return iconStyle;
    }

    public Style setIconStyle(final ItemStyle iconStyle) {
        this.iconStyle = iconStyle;
        return this;
    }

    public ItemStyle getLabelStyle() {
        return labelStyle;
    }

    public Style setLabelStyle(final ItemStyle labelStyle) {
        this.labelStyle = labelStyle;
        return this;
    }

    public ItemStyle getLineStyle() {
        return lineStyle;
    }

    public Style setLineStyle(final ItemStyle lineStyle) {
        this.lineStyle = lineStyle;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("iconStyle", iconStyle)
                .append("labelStyle", labelStyle)
                .append("lineStyle", lineStyle)
                .toString();
    }
}
