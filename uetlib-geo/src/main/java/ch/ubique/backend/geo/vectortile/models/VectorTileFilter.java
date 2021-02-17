package ch.ubique.backend.geo.vectortile.models;

public interface VectorTileFilter<T> {

	public boolean accept(T tile);
}
