package ch.ubique.backend.geo.vectortile.uploader;

import ch.ubique.backend.geo.vectortile.models.BinaryTile;
import java.util.Collection;


public interface VectorTileUploader {

	void upload(String path, Collection<BinaryTile> binaryTiles);
	
}
