package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FilteredVectorTileElementSupplier<T extends VectorTileElement>
        implements Function<TileInfo, Collection<VectorTileElement>> {

    private final VectorTileElementSupplier<T> elementSupplier;

    private final Predicate<T> filter;

    public FilteredVectorTileElementSupplier(
            final VectorTileElementSupplier<T> elementSupplier, final Predicate<T> filter) {
        this.elementSupplier = elementSupplier;
        this.filter = filter;
    }

    public Collection<VectorTileElement> getElements(final TileInfo tileInfo) {
        return elementSupplier
                .getElements(tileInfo)
                .filter(filter)
                .map(VectorTileElement.class::cast)
                .toList();
    }

    @Override
    public Collection<VectorTileElement> apply(final TileInfo tileInfo) {
        return getElements(tileInfo);
    }
}
