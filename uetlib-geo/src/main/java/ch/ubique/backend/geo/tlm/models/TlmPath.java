package ch.ubique.backend.geo.tlm.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;

import java.util.List;

public class TlmPath {

    private String uuid;

    private WegType type;

    private WanderwegType wanderwegType;

    private Integer stufe;

    private BelagType belagsart;

    private Verkehrsbedeutung verkehrsbedeutung;

    private List<LV95Coordinate> polyline;


    public WegType getType() {
        return type;
    }

    public void setType(WegType type) {
        this.type = type;
    }

    public WanderwegType getWanderwegType() {
        return wanderwegType;
    }

    public void setWanderwegType(WanderwegType wanderwegType) {
        this.wanderwegType = wanderwegType;
    }

    public Integer getStufe() {
        return stufe;
    }

    public void setStufe(Integer stufe) {
        this.stufe = stufe;
    }

    public BelagType getBelagsart() {
        return belagsart;
    }

    public void setBelagsart(BelagType belagsart) {
        this.belagsart = belagsart;
    }

    public Verkehrsbedeutung getVerkehrsbedeutung() {
        return verkehrsbedeutung;
    }

    public void setVerkehrsbedeutung(Verkehrsbedeutung verkehrsbedeutung) {
        this.verkehrsbedeutung = verkehrsbedeutung;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isWanderweg() {
        return wanderwegType != null;
    }

    public void setPolyline(List<LV95Coordinate> polyline) {
        this.polyline = polyline;
    }


    public List<LV95Coordinate> getPolyline() {
        return polyline;
    }

    @Override
    public String toString() {
        return type + ", " + polyline;
    }

    @Override
    public boolean equals(Object o) {
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
