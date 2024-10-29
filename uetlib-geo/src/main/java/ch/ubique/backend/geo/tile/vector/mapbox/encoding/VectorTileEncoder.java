package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileLayer;
import ch.ubique.backend.geo.vectortile.protobuf.Tile;

import ch.ubique.backend.geo.vectortile.protobuf.Tile.Layer;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

/**
 * Encodes vector tiles according to <a href="https://github.com/mapbox/vector-tile-spec">Mapbox
 * Vector Tile Specification</a>
 */
public class VectorTileEncoder {

    private final Map<String, LayerEncoder> layerEncoders = new LinkedHashMap<>();

    private final int extent;

    public VectorTileEncoder(final int extent) {
        this.extent = extent;
    }

    /**
     * The Geometry must be in tile coordinate space (0,0) upper left and (extent,extent) lower
     * right.
     */
    public void addFeature(
            final String layerName,
            final Map<String, ?> attributes,
            final Geometry geometry,
            final long id) {

        final var layerEncoder =
                layerEncoders.computeIfAbsent(layerName, name -> new LayerEncoder(name, extent));

        layerEncoder.addFeature(id, geometry, attributes);
    }

    public byte[] encode() {
        final var encodedLayers =
                layerEncoders.values().stream().map(LayerEncoder::encode).toList();
        return Tile.newBuilder().addAllLayers(encodedLayers).build().toByteArray();
    }

    public static Tile encode(final Collection<VectorTileLayer> layers) {
        final var encodedLayers = layers.stream().map(VectorTileEncoder::encodeLayer).toList();
        return Tile.newBuilder().addAllLayers(encodedLayers).build();
    }

    private static Layer encodeLayer(final VectorTileLayer layer) {
        final var layerEncoder = new LayerEncoder(layer.name(), layer.extent());
        for (final var feature : layer.features()) {
            layerEncoder.addFeature(feature.id(), feature.geometry(), feature.tags());
        }
        return layerEncoder.encode();
    }
}
