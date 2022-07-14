package ch.ubique.backend.geo.vectortile.models;

import ch.ubique.backend.geo.geometry.models.BoundingBox2d;
import java.util.ArrayList;
import java.util.Collection;

public class Tile<T> {

    private final TileInfo tileInfo;
    private final BoundingBox2d boundingBox;
    private final Collection<T> elements;

    public Tile(TileInfo tileInfo, BoundingBox2d boundingBox) {
        this(tileInfo, boundingBox, new ArrayList<>());
    }

    public Tile(TileInfo tileInfo, BoundingBox2d boundingBox, Collection<T> elements) {
        this.tileInfo = tileInfo;
        this.boundingBox = boundingBox;
        this.elements = elements;
    }

    public BoundingBox2d getBoundingBox() {
        return boundingBox;
    }

    public TileInfo getTileInfo() {
        return tileInfo;
    }

    public Collection<T> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile<T> tile = (Tile<T>) o;
        return boundingBox.equals(tile.boundingBox);
    }

    @Override
    public int hashCode() {
        return boundingBox.hashCode();
    }
}
