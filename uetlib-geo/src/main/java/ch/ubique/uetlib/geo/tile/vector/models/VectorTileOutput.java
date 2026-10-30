package ch.ubique.uetlib.geo.tile.vector.models;

import ch.ubique.uetlib.core.utils.ValidationUtils;
import ch.ubique.uetlib.geo.tile.common.models.TileInfo;
import ch.ubique.uetlib.geo.tile.vector.utils.VectorTileElementSupplier;
import com.pivovarit.function.ThrowingConsumer;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        final var elements = elementSupplier.apply(tileInfo);
        if (config.validateElements()) {
            elements.forEach(ValidationUtils::requireIsValid);
        }
        return elements;
    }
}
