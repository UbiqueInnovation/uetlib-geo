package ch.ubique.backend.geo.kml.models;

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
        return "Icon{" + "href='" + href + '\'' + '}';
    }
}
