package ch.ubique.backend.geo.tile.vector.models;

public interface VectorTileFilter<T> {

    public boolean accept(T tile);
}
