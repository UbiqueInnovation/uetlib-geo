package ch.ubique.backend.geo.tile.vector.mapbox.encoding;

import ch.ubique.backend.geo.vectortile.protobuf.Tile;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import java.util.*;
import java.util.stream.Stream;

class LayerEncoder {

    private final Layer layer;

    public LayerEncoder(final String name, final int extent) {
        this.layer = new Layer(name, extent);
    }

    /**
     * The Geometry must be in tile coordinate space (0,0) upper left and (extent,extent) lower
     * right.
     */
    public void addFeature(
            final long id, final Geometry geometry, final Map<String, ?> attributes) {

        // no need to add empty geometry
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        // Unwrap geometry collection
        if (geometry.getClass().equals(GeometryCollection.class)) {
            for (var i = 0; i < geometry.getNumGeometries(); i++) {
                addFeature(id, geometry.getGeometryN(i), attributes);
            }
            return;
        }

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

    Tile.Layer encode() {
        return Tile.Layer.newBuilder()
                .setVersion(2)
                .setName(layer.name)
                .setExtent(layer.extent)
                .addAllKeys(layer.keys())
                .addAllValues(layer.values().stream().map(ValueEncoder::encodeValue).toList())
                .addAllFeatures(layer.features().stream().map(this::encodeFeature).toList())
                .build();
    }

    private Tile.Feature encodeFeature(final Feature feature) {
        final var geometryEncoder = new GeometryEncoder(feature.geometry);
        final var builder = Tile.Feature.newBuilder();
        if (feature.id != 0) {
            builder.setId(feature.id);
        }
        return builder.setType(geometryEncoder.getGeomType())
                .addAllGeometry(CommandsEncoder.encode(geometryEncoder.encode()))
                .addAllTags(feature.tags)
                .build();
    }

    private record Layer(
            String name,
            int extent,
            List<LayerEncoder.Feature> features,
            Map<String, Integer> keyMap,
            Map<Object, Integer> valueMap) {

        private Layer(final String name, final int extent) {
            this(name, extent, new ArrayList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        private Integer indexOfKey(final String key) {
            return keyMap.computeIfAbsent(key, ignored -> keyMap.size());
        }

        private Integer indexOfValue(final Object value) {
            return valueMap.computeIfAbsent(value, ignored -> valueMap.size());
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
