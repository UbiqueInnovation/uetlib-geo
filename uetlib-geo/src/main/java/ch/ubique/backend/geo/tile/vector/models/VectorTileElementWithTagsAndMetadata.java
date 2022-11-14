package ch.ubique.backend.geo.tile.vector.models;

public interface VectorTileElementWithTagsAndMetadata<T, M> extends
        VectorTileElementWithTags<T>,
        VectorTileElementWithMetadata<M> {
}
