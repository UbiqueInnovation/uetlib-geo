package ch.uetlib.geo.tile.vector.models;

import ch.uetlib.geo.tile.common.models.TileInfo;
import ch.uetlib.geo.tile.common.models.TileOutputFormat;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

// equals and hashCode are overridden, so the array components are not compared by reference.
@SuppressWarnings("ArrayRecordComponent")
public record BinaryTile(TileInfo info, TileOutputFormat format, byte[] bytes) {

    @SuppressWarnings("unused")
    public static final String contentType = "application/protobuf";

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final BinaryTile that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(info, that.info)
                .append(format, that.format)
                .append(bytes, that.bytes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(info).append(format).append(bytes).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("info", info)
                .append("format", format)
                .append("bytes.length", bytes.length)
                .toString();
    }
}
