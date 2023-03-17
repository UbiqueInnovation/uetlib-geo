package ch.ubique.backend.geo.tlm.models;

import org.locationtech.jts.geom.LineString;

public class TlmPath {

    private String uuid;

    private WegType type;

    private WanderwegType wanderwegType;

    private Integer stufe;

    private BelagType belagsart;

    private Verkehrsbedeutung verkehrsbedeutung;

    private LineString line;


    public WegType getType() {
        return type;
    }

    public void setType(final WegType type) {
        this.type = type;
    }

    public WanderwegType getWanderwegType() {
        return wanderwegType;
    }

    public void setWanderwegType(final WanderwegType wanderwegType) {
        this.wanderwegType = wanderwegType;
    }

    public Integer getStufe() {
        return stufe;
    }

    public void setStufe(final Integer stufe) {
        this.stufe = stufe;
    }

    public BelagType getBelagsart() {
        return belagsart;
    }

    public void setBelagsart(final BelagType belagsart) {
        this.belagsart = belagsart;
    }

    public Verkehrsbedeutung getVerkehrsbedeutung() {
        return verkehrsbedeutung;
    }

    public void setVerkehrsbedeutung(final Verkehrsbedeutung verkehrsbedeutung) {
        this.verkehrsbedeutung = verkehrsbedeutung;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public boolean isWanderweg() {
        return wanderwegType != null;
    }

    public LineString getLine() {
        return line;
    }

    public void setLine(final LineString line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return type + ", " + line;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var path = (TlmPath) o;
        return uuid.equals(path.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
