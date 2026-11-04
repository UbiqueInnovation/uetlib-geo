package ch.uetlib.geo.tile.vector.mapbox.decoding;

import ch.uetlib.geo.tile.vector.mapbox.models.VectorTile;
import ch.uetlib.geo.tile.vector.mapbox.models.VectorTileLayer;
import ch.uetlib.geo.vectortile.protobuf.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Decodes vector tiles according to <a href="https://github.com/mapbox/vector-tile-spec">Mapbox
 * Vector Tile Specification</a>
 */
public class VectorTileDecoder {

    private VectorTileDecoder() {}

    public static VectorTile decode(final byte[] bytes) throws IOException {
        return decode(Tile.parseFrom(bytes));
    }

    public static VectorTile decode(final InputStream inputStream) throws IOException {
        return decode(Tile.parseFrom(inputStream));
    }

    public static VectorTile decode(final Tile tile) throws UnsupportedEncodingException {
        final var layers = new ArrayList<VectorTileLayer>(tile.getLayersCount());
        for (final var layer : tile.getLayersList()) {
            layers.add(LayerDecoder.decode(layer));
        }
        return new VectorTile(Collections.unmodifiableList(layers));
    }
}
