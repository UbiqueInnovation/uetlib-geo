package ch.ubique.backend.geo.vectortile.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record BinaryTile(TileInfo info, byte[] bytes) {

    public static final String contentType = "application/protobuf";

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("info", info)
                .append("bytes.length", bytes.length)
                .toString();
    }
}
