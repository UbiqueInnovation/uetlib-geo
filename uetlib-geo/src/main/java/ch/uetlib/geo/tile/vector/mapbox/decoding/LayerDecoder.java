package ch.uetlib.geo.tile.vector.mapbox.decoding;

import ch.uetlib.geo.tile.vector.mapbox.models.VectorTileFeature;
import ch.uetlib.geo.tile.vector.mapbox.models.VectorTileLayer;
import ch.uetlib.geo.vectortile.protobuf.Tile.Feature;
import ch.uetlib.geo.vectortile.protobuf.Tile.Layer;
import ch.uetlib.geo.vectortile.protobuf.Tile.Value;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LayerDecoder {

    private LayerDecoder() {}

    static VectorTileLayer decode(final Layer layer) throws UnsupportedEncodingException {
        final var version = layer.getVersion();
        if (version != 2) {
            throw new UnsupportedEncodingException("Unsupported layer version: " + version);
        }

        final var name = layer.getName();
        final var extent = layer.getExtent();
        final var keys = layer.getKeysList();
        final var values = layer.getValuesList().stream().map(LayerDecoder::decodeValue).toList();

        final var features = new ArrayList<VectorTileFeature>(layer.getFeaturesCount());
        for (final var feature : layer.getFeaturesList()) {
            features.add(decodeFeature(feature, keys, values));
        }
        return new VectorTileLayer(extent, name, Collections.unmodifiableList(features));
    }

    private static VectorTileFeature decodeFeature(
            final Feature feature, final List<String> keys, final List<Object> values) {
        final var tags = decodeTags(feature.getTagsList(), keys, values);
        final var commands = CommandsDecoder.COMMANDS_DECODER_2D.decode(feature.getGeometryList());
        final var geometry = GeometryDecoder.decode(feature.getType(), commands);
        return new VectorTileFeature(feature.getId(), geometry, tags);
    }

    private static Map<String, Object> decodeTags(
            final List<Integer> tagPairs, final List<String> keys, final List<Object> values) {
        final var map = HashMap.<String, Object>newHashMap(tagPairs.size() / 2);
        for (var i = 0; i + 1 < tagPairs.size(); i += 2) {
            final var keyIndex = tagPairs.get(i);
            final var valueIndex = tagPairs.get(i + 1);
            final var key = keys.get(keyIndex);
            final var value = values.get(valueIndex);
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Object decodeValue(final Value value) {
        if (value.hasStringValue()) {
            return value.getStringValue();
        }
        if (value.hasFloatValue()) {
            return value.getFloatValue();
        }
        if (value.hasDoubleValue()) {
            return value.getDoubleValue();
        }
        if (value.hasIntValue()) {
            return value.getIntValue();
        }
        if (value.hasUintValue()) {
            return value.getUintValue();
        }
        if (value.hasSintValue()) {
            return value.getSintValue();
        }
        if (value.hasBoolValue()) {
            return value.getBoolValue();
        }
        throw new IllegalStateException("No value present.");
    }
}
