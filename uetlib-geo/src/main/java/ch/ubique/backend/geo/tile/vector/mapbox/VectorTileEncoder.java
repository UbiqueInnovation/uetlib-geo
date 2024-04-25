package ch.ubique.backend.geo.tile.vector.mapbox;

import ch.ubique.backend.geo.vectortile.protobuf.Tile;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import java.util.*;
import java.util.stream.Stream;

/**
 * Encodes vector tiles according to <a href="https://github.com/mapbox/vector-tile-spec">Mapbox
 * Vector Tile Specification</a>
 */
public class VectorTileEncoder {

    private final Map<String, Layer> layers = new LinkedHashMap<>();

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

        // no need to add empty geometry
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        // Unwrap geometry collection
        if (geometry.getClass().equals(GeometryCollection.class)) {
            for (var i = 0; i < geometry.getNumGeometries(); i++) {
                addFeature(layerName, attributes, geometry.getGeometryN(i), id);
            }
            return;
        }

        final var layer = layers.computeIfAbsent(layerName, ignored -> new Layer());

        final var tags =
                attributes.entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .flatMap(entry -> getTags(entry, layer))
                        .toList();

        layer.features.add(new Feature(id, geometry, tags));
    }

    private static Stream<Integer> getTags(final Map.Entry<String, ?> entry, final Layer layer) {
        return Stream.of(layer.indexOfKey(entry.getKey()), layer.indexOfValue(entry.getValue()));
    }

    public byte[] encode() {
        final var encodedLayers =
                layers.entrySet().stream().map(e -> encodeLayer(e.getKey(), e.getValue())).toList();
        return Tile.newBuilder().addAllLayers(encodedLayers).build().toByteArray();
    }

    private Tile.Layer encodeLayer(final String layerName, final Layer layer) {
        return Tile.Layer.newBuilder()
                .setVersion(2)
                .setName(layerName)
                .setExtent(extent)
                .addAllKeys(layer.keys())
                .addAllValues(layer.values().stream().map(this::encodeValue).toList())
                .addAllFeatures(layer.features().stream().map(this::encodeFeature).toList())
                .build();
    }

    private Tile.Feature encodeFeature(final Feature feature) {
        final var geometryEncoder = new GeometryEncoder(feature.geometry);
        return Tile.Feature.newBuilder()
                .setId(feature.id)
                .setType(geometryEncoder.getGeomType())
                .addAllGeometry(CommandsEncoder.encode(geometryEncoder.encode()))
                .addAllTags(feature.tags)
                .build();
    }

    private Tile.Value encodeValue(final Object value) {
        final var tileValue = Tile.Value.newBuilder();
        switch (value) {
            case final String string -> tileValue.setStringValue(string);
            case final Integer intValue -> tileValue.setSintValue(intValue);
            case final Long longValue -> tileValue.setSintValue(longValue);
            case final Float floatValue -> tileValue.setFloatValue(floatValue);
            case final Double doubleValue -> tileValue.setDoubleValue(doubleValue);
            case final Number number -> tileValue.setStringValue(number.toString());
            case final Boolean booleanValue -> tileValue.setBoolValue(booleanValue);
            default -> tileValue.setStringValue(value.toString());
        }
        return tileValue.build();
    }

    private record Layer(
            List<Feature> features, Map<String, Integer> keyMap, Map<Object, Integer> valueMap) {

        private Layer() {
            this(new ArrayList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        private Integer indexOfKey(final String key) {
            return keyMap.computeIfAbsent(key, ignored -> keyMap.size());
        }

        private Integer indexOfValue(final Object value) {
            return valueMap.computeIfAbsent(value, ignored -> valueMap.size() - 1);
        }

        private Collection<String> keys() {
            return keyMap.keySet();
        }

        private Collection<Object> values() {
            return valueMap.keySet();
        }
    }

    private record Feature(long id, Geometry geometry, List<Integer> tags) {}
}
