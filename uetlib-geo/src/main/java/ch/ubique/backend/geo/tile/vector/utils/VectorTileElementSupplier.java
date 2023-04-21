package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;

import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class VectorTileElementSupplier<T extends VectorTileElement>
        implements Function<TileInfo, Collection<VectorTileElement>> {

    private final SpatialIndex spatialIndex;

    public VectorTileElementSupplier(final Collection<? extends T> elements) {
        this(elements.stream());
    }

    public VectorTileElementSupplier(final Stream<? extends T> elements) {
        spatialIndex = new Quadtree();
        elements.forEach(element -> spatialIndex.insert(element.boundingBox(), element));
    }

    public Stream<T> getElements(final TileInfo tileInfo) {
        //noinspection unchecked
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
