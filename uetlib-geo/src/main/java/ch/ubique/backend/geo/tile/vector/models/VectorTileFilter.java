package ch.ubique.backend.geo.tile.vector.models;

/**
 * @deprecated Please override the {@link VectorTileElement#showElement(int)}
 * to control when and where a vector tile element is filtered.
 * Or maybe don't even pass the elements you want to filter to your {@link VectorTileOutput}.
 * Will be removed with version 0.9.0
 */
@Deprecated(since = "0.8.0", forRemoval = true)
public interface VectorTileFilter<T> {

    public boolean accept(T tile);
}
