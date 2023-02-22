package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;

public class VectorTileElementSupplier<T extends VectorTileElement>
        implements Function<TileInfo, Collection<T>> {

    private final SpatialIndex spatialIndex;

    public VectorTileElementSupplier(final Collection<? extends T> elements) {
        this(elements.stream());
    }

    public VectorTileElementSupplier(final Stream<? extends T> elements) {
        spatialIndex = new Quadtree();
        elements.forEach(element -> spatialIndex.insert(element.boundingBox(), element));
    }

    public Collection<T> getElements(final TileInfo tileInfo) {
        //noinspection unchecked
        return spatialIndex.query(tileInfo.envelope());
    }

    @Override
    public Collection<T> apply(final TileInfo tileInfo) {
        return getElements(tileInfo);
    }
}
