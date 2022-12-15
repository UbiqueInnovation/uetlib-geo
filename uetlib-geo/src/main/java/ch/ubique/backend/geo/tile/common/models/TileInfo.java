package ch.ubique.backend.geo.tile.common.models;

import org.geotools.geometry.jts.ReferencedEnvelope;

public record TileInfo(int x, int y, ZoomLevel zoomLevel, ReferencedEnvelope envelope) {

    public TileInfo(
            final int x,
            final int y,
            final ZoomLevel level,
            final TileLevelsDefinition tileLevelsDefinition) {
        this(x, y, level, tileLevelsDefinition.envelopeOf(level, x, y));
    }

    public String path() {
        return level() + "/" + x + "/" + y + ".pbf";
    }

    public int level() {
        return zoomLevel.level();
    }

    @Override
    public String toString() {
        return zoomLevel.toString() + "_" + x + "_" + y;
    }
}
