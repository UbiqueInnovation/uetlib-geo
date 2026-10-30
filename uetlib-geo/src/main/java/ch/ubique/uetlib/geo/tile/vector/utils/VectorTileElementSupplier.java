package ch.ubique.uetlib.geo.tile.vector.utils;

import ch.ubique.uetlib.geo.spatial.TypedQuadtree;
import ch.ubique.uetlib.geo.tile.common.models.TileInfo;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileElement;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class VectorTileElementSupplier<T extends VectorTileElement>
        implements Function<TileInfo, Collection<VectorTileElement>> {

    private final TypedQuadtree<T> spatialIndex;

    public VectorTileElementSupplier(final Collection<? extends T> elements) {
        this(elements.stream());
    }

    public VectorTileElementSupplier(final Stream<? extends T> elements) {
        spatialIndex = new TypedQuadtree<>(elements, VectorTileElement::boundingBox);
    }

    public Stream<T> getElements(final TileInfo tileInfo) {
        return spatialIndex.query(tileInfo.envelope()).stream();
    }

    @Override
    public Collection<VectorTileElement> apply(final TileInfo tileInfo) {
        return getElements(tileInfo).map(VectorTileElement.class::cast).toList();
    }

    public FilteredVectorTileElementSupplier<T> withFilter(final Predicate<T> predicate) {
        return new FilteredVectorTileElementSupplier<>(this, predicate);
    }
}
