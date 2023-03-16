package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementSupplier;

import com.pivovarit.function.ThrowingConsumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public record VectorTileOutput(
        VectorTileConfig config,
        Function<TileInfo, Collection<VectorTileElement>> elementSupplier,
        ThrowingConsumer<BinaryTile, IOException> tileConsumer)
        implements TileOutput {

    public VectorTileOutput(
            final VectorTileConfig config,
            final Collection<VectorTileElement> elements,
            final ThrowingConsumer<BinaryTile, IOException> tileConsumer) {
        this(config, new VectorTileElementSupplier<>(elements), tileConsumer);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("config", config)
                .append("elementSupplier", elementSupplier)
                .append("tileConsumer", tileConsumer)
                .toString();
    }

    public Collection<VectorTileElement> elements(final TileInfo tileInfo) {
        return elementSupplier.apply(tileInfo);
    }
}
