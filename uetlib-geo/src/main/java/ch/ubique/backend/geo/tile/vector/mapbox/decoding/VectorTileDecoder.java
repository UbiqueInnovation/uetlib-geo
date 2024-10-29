package ch.ubique.backend.geo.tile.vector.mapbox.decoding;

import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileLayer;
import ch.ubique.backend.geo.vectortile.protobuf.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Decodes vector tiles according to <a href="https://github.com/mapbox/vector-tile-spec">Mapbox
 * Vector Tile Specification</a>
 */
public class VectorTileDecoder {

    private VectorTileDecoder() {}

    public static Collection<VectorTileLayer> decode(final byte[] bytes) throws IOException {
        return decode(Tile.parseFrom(bytes));
    }

    public static Collection<VectorTileLayer> decode(final InputStream inputStream)
            throws IOException {
        return decode(Tile.parseFrom(inputStream));
    }

    public static Collection<VectorTileLayer> decode(final Tile tile)
            throws UnsupportedEncodingException {
        final var layers = new ArrayList<VectorTileLayer>(tile.getLayersCount());
        for (final var layer : tile.getLayersList()) {
            layers.add(LayerDecoder.decode(layer));
        }
        return Collections.unmodifiableList(layers);
    }
}
