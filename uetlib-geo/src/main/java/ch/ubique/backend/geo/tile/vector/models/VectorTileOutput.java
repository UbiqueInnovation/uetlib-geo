package ch.ubique.backend.geo.tile.vector.models;

import com.pivovarit.function.ThrowingConsumer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.util.Collection;

public record VectorTileOutput<T extends VectorTileElement>(
        VectorTileConfig<T> config,
        Collection<T> elements,
        ThrowingConsumer<BinaryTile, IOException> tileConsumer
) {
    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("config", config)
                .append("elements.size", elements == null ? "null" : elements.size())
                .toString();
    }
}
