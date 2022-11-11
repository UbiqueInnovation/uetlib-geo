package ch.ubique.backend.geo.tile.vector.models;

public interface VectorTileElementUnweightedAlwaysVisible<T, M> extends VectorTileElement<T, M> {

    @Override
    default double score() {
        return 0;
    }

    @Override
    default boolean showElement(int zoomLevel) {
        return true;
    }
}
