package ch.ubique.backend.geo.mbtile;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

/** https://gist.github.com/tmcw/4954720 (XYZ vs. TMS) */
public class TmsTile {
    private int zoomLevel;
    private int column;
    private int row;
    private byte[] data;

    public TmsTile(int zoomLevel, int column, int row, byte[] data) {
        this.zoomLevel = zoomLevel;
        this.column = column;
        this.row = row;
        this.data = data;
    }

    public TmsTile(TileInfo tileInfo, byte[] data) {
        this.zoomLevel = tileInfo.zoomLevel().level();
        this.column = tileInfo.x();
        this.row = getRowForXyzTileInfo(tileInfo);
        this.data = data;
    }

    public TmsTile(BinaryTile tile) {
        this.zoomLevel = tile.info().zoomLevel().level();
        this.column = tile.info().x();
        this.row = getRowForXyzTileInfo(tile.info());
        this.data = tile.bytes();
    }

    private static int getRowForXyzTileInfo(TileInfo tileInfo) {
        return (int) Math.pow(2, tileInfo.zoomLevel().level()) - tileInfo.y() - 1;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
