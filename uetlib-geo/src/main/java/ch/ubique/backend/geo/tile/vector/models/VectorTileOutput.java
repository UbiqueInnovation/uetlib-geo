package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import com.pivovarit.function.ThrowingConsumer;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record VectorTileOutput(
        VectorTileConfig config,
        Collection<VectorTileElement> elements,
        Function<TileInfo, Collection<VectorTileElement>> elementSupplier,
        ThrowingConsumer<BinaryTile, IOException> tileConsumer)
        implements TileOutput {

    public VectorTileOutput(
            final VectorTileConfig config,
            final Function<TileInfo, Collection<VectorTileElement>> elementSupplier,
            final ThrowingConsumer<BinaryTile, IOException> tileConsumer) {
        this(config, null, elementSupplier, tileConsumer);
    }

    public VectorTileOutput(
            final VectorTileConfig config,
            final Collection<VectorTileElement> elements,
            final ThrowingConsumer<BinaryTile, IOException> tileConsumer) {
        this(config, elements, null, tileConsumer);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("config", config)
                .append("elements", elements != null ? elements.size() : null)
                .append("elementSupplier", elementSupplier)
                .append("tileConsumer", tileConsumer)
                .toString();
    }

    public boolean usesElementSupplier() {
        return elementSupplier != null;
    }

    public Collection<VectorTileElement> elements(final TileInfo tileInfo) {
        return usesElementSupplier() ? elementSupplier.apply(tileInfo) : elements;
    }
}
