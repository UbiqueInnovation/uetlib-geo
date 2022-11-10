package ch.ubique.backend.geo.tile.vector.models;

import org.locationtech.jts.geom.Geometry;

@SuppressWarnings("ClassCanBeRecord") // We want to allow inheriting from this class -> it can not be a record
public class BaseVectorTileElement<T, M> implements VectorTileElement<T, M> {

    private final long id;
    private final String layer;
    private final double score;
    private final T tags;
    private final M metadata;
    private final Geometry geometry;

    public BaseVectorTileElement(long id, String layer, double score, T tags, M metadata, Geometry geometry) {
        this.id = id;
        this.layer = layer;
        this.score = score;
        this.tags = tags;
        this.metadata = metadata;
        this.geometry = geometry;
    }

    @Override
    public final long id() {
        return id;
    }

    @Override
    public final String layer() {
        return layer;
    }

    @Override
    public final double score() {
        return score;
    }

    @Override
    public final T tags() {
        return tags;
    }

    @Override
    public final M metadata() {
        return metadata;
    }

    @Override
    public boolean showElement(int zoomLevel) {
        return true;
    }

    @Override
    public final Geometry geometry() {
        return geometry;
    }

}
