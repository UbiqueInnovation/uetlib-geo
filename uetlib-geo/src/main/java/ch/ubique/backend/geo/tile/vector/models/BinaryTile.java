package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record BinaryTile(TileInfo info, TileOutputFormat format, byte[] bytes) {

    public static final String contentType = "application/protobuf";

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("info", info)
                .append("format", format)
                .append("bytes.length", bytes.length)
                .toString();
    }
}
