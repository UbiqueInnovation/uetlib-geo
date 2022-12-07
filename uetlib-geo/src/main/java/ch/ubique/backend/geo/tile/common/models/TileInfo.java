package ch.ubique.backend.geo.tile.common.models;

import org.geotools.geometry.jts.ReferencedEnvelope;

public record TileInfo(int x, int y, ZoomLevel zoomLevel) {

    public String path() {
        return level() + "/" + x + "/" + y + ".pbf";
    }

    public ReferencedEnvelope envelope(final TileLevelsDefinition tileLevelsDefinition) {
        return tileLevelsDefinition.envelopeOf(level(), x, y);
    }

    public int level() {
        return zoomLevel.level();
    }

    @Override
    public String toString() {
        return zoomLevel.toString() + "_" + x + "_" + y;
    }
}
