package ch.ubique.uetlib.geo.kml.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Icon {
    private String href;

    public String getHref() {
        return href;
    }

    public Icon setHref(final String href) {
        this.href = href;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("href", href)
                .toString();
    }
}
