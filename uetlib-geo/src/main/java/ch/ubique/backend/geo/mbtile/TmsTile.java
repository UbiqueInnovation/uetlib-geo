package ch.ubique.backend.geo.mbtile;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

/** https://gist.github.com/tmcw/4954720 (XYZ vs. TMS) */
public record TmsTile(int zoomLevel, int column, int row, byte[] data) {

    public TmsTile(int zoomLevel, int column, int row, byte[] data) {
        this.zoomLevel = zoomLevel;
        this.column = column;
        this.row = row;
        this.data = data;
    }

    public TmsTile(TileInfo tileInfo, byte[] data) {
        this(tileInfo.zoomLevel().level(), tileInfo.x(), getRowForXyzTileInfo(tileInfo), data);
    }

    public TmsTile(BinaryTile tile) {
        this(
                tile.info().zoomLevel().level(),
                tile.info().x(),
                getRowForXyzTileInfo(tile.info()),
                tile.bytes());
    }

    private static int getRowForXyzTileInfo(TileInfo tileInfo) {
        return (int) Math.pow(2, tileInfo.zoomLevel().level()) - tileInfo.y() - 1;
    }
}
