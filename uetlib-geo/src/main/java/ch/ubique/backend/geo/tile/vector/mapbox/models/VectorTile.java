package ch.ubique.backend.geo.tile.vector.mapbox.models;

import java.util.Collection;

public record VectorTile(Collection<VectorTileLayer> layers) {}
