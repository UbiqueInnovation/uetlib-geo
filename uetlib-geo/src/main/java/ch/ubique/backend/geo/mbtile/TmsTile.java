package ch.ubique.backend.geo.mbtile;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** <a href="https://gist.github.com/tmcw/4954720">GitHub Gist</a> (XYZ vs. TMS) */
public record TmsTile(int zoomLevel, int column, int row, byte[] data) {

    public TmsTile(final TileInfo tileInfo, final byte[] data) {
        this(tileInfo.zoomLevel().level(), tileInfo.x(), getRowForXyzTileInfo(tileInfo), data);
    }

    public TmsTile(final BinaryTile tile) {
        this(
                tile.info().zoomLevel().level(),
                tile.info().x(),
                getRowForXyzTileInfo(tile.info()),
                tile.bytes());
    }

    private static int getRowForXyzTileInfo(final TileInfo tileInfo) {
        return (int) Math.pow(2, tileInfo.zoomLevel().level()) - tileInfo.y() - 1;
    }

    @SuppressWarnings("java:S6878")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        //noinspection DeconstructionCanBeUsed
        if (o instanceof final TmsTile tmsTile) {
            return new EqualsBuilder()
                    .append(zoomLevel, tmsTile.zoomLevel)
                    .append(column, tmsTile.column)
                    .append(row, tmsTile.row)
                    .append(data, tmsTile.data)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(zoomLevel)
                .append(column)
                .append(row)
                .append(data)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("zoomLevel", zoomLevel)
                .append("column", column)
                .append("row", row)
                .append("data", data)
                .toString();
    }
}
