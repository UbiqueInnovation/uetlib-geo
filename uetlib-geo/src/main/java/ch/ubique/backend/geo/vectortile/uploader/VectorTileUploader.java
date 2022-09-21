package ch.ubique.backend.geo.vectortile.uploader;

import ch.ubique.backend.geo.vectortile.models.BinaryTile;

import java.util.Collection;

public interface VectorTileUploader {

    void prepareUpload(String path);

    void upload(String path, BinaryTile binaryTile);

    void upload(String path, Collection<BinaryTile> binaryTiles);
}
