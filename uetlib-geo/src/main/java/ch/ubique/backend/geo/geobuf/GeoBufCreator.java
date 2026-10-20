package ch.ubique.backend.geo.geobuf;

import static java.util.Objects.requireNonNull;

import ch.ubique.backend.geo.geobuf.protobuf.Data;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encodes GeoJSON using the Mapbox GeoBuf schema, delta-encoded coordinates and shared property
 * keys. Precision is expressed in the input coordinate system; no coordinate transformation is
 * performed.
 */
public final class GeoBufCreator {
    private final int precision;

    public GeoBufCreator() {
        this(6);
    }

    public GeoBufCreator(final int precision) {
        if (precision < 0 || precision > 9) {
            throw new IllegalArgumentException("GeoBuf precision must be between 0 and 9");
        }
        this.precision = precision;
    }

    /** Retains feature IDs, properties and foreign members such as CRS and bounding boxes. */
    public byte[] toGeoBufNative(final JsonNode geoJson) {
        return new Encoder(precision).encode(geoJson);
    }

    private static final class Encoder {
        private static final String COORDINATES = "coordinates";
        private static final String FEATURE = "Feature";
        private static final String GEOMETRY_COLLECTION = "GeometryCollection";
        private static final String FEATURES = "features";
        private static final String FEATURE_COLLECTION = "FeatureCollection";
        private static final String GEOMETRIES = "geometries";
        private static final String GEOMETRY = "geometry";
        private static final String POLYGON = "Polygon";
        private static final String POINT = "Point";
        private static final String MULTI_POINT = "MultiPoint";
        private static final String LINE_STRING = "LineString";
        private static final String MULTI_LINE_STRING = "MultiLineString";
        private static final String MULTI_POLYGON = "MultiPolygon";
        private static final String PROPERTIES = "properties";
        private static final Map<String, Data.Geometry.Type> GEOMETRY_TYPES =
                Map.of(
                        POINT,
                        Data.Geometry.Type.POINT,
                        MULTI_POINT,
                        Data.Geometry.Type.MULTIPOINT,
                        LINE_STRING,
                        Data.Geometry.Type.LINESTRING,
                        MULTI_LINE_STRING,
                        Data.Geometry.Type.MULTILINESTRING,
                        POLYGON,
                        Data.Geometry.Type.POLYGON,
                        MULTI_POLYGON,
                        Data.Geometry.Type.MULTIPOLYGON,
                        GEOMETRY_COLLECTION,
                        Data.Geometry.Type.GEOMETRYCOLLECTION);
        private static final Set<String> FEATURE_MEMBERS =
                Set.of("type", "id", PROPERTIES, GEOMETRY);
        private final int precision;
        private final double scale;
        private final Map<String, Integer> keys = new LinkedHashMap<>();
        private int dimensions;

        Encoder(final int precision) {
            this.precision = precision;
            scale = Math.pow(10, precision);
        }

        byte[] encode(final JsonNode json) {
            // First pass: infer dimensions and store each property name once in a shared
            // dictionary.
            analyze(json);
            final var data =
                    Data.newBuilder()
                            .setPrecision(precision)
                            .setDimensions(dimensions == 0 ? 2 : dimensions)
                            .addAllKeys(keys.keySet());
            // Protocol Buffers packs the message, including signed coordinate deltas, into bytes.
            return serialize(
                            output -> {
                                data.build().writeTo(output);
                                switch (type(json)) {
                                    case FEATURE_COLLECTION ->
                                            output.writeBytes(
                                                    Data.FEATURE_COLLECTION_FIELD_NUMBER,
                                                    collection(json));
                                    case FEATURE ->
                                            output.writeBytes(
                                                    Data.FEATURE_FIELD_NUMBER, feature(json));
                                    default ->
                                            output.writeMessage(
                                                    Data.GEOMETRY_FIELD_NUMBER, geometry(json));
                                }
                            })
                    .toByteArray();
        }

        private void analyze(final JsonNode node) {
            if (!node.isObject()) {
                throw new IllegalArgumentException("Expected a GeoJSON object");
            }
            switch (type(node)) {
                case FEATURE_COLLECTION -> analyzeFeatureCollection(node);
                case FEATURE -> analyzeFeature(node);
                case GEOMETRY_COLLECTION -> {
                    if (!node.path(GEOMETRIES).isArray()) {
                        throw new IllegalArgumentException("Expected a GeoJSON geometries array");
                    }
                    for (final var geometry : node.path(GEOMETRIES)) {
                        analyze(geometry);
                    }
                }
                default -> {
                    final int depth =
                            switch (type(node)) {
                                case POINT -> 0;
                                case MULTI_POINT, LINE_STRING -> 1;
                                case MULTI_LINE_STRING, POLYGON -> 2;
                                case MULTI_POLYGON -> 3;
                                default ->
                                        throw new IllegalArgumentException(
                                                "Unknown GeoJSON geometry: " + type(node));
                            };
                    analyzeCoordinates(node.path(COORDINATES), depth);
                }
            }
            for (final var property : node.properties()) {
                if (!special(property.getKey(), type(node))) {
                    saveKey(property.getKey());
                }
            }
        }

        private void analyzeFeatureCollection(final JsonNode node) {
            if (!node.path(FEATURES).isArray()) {
                throw new IllegalArgumentException("Expected a GeoJSON features array");
            }
            for (final var feature : node.path(FEATURES)) {
                if (!FEATURE.equals(type(feature))) {
                    throw new IllegalArgumentException("Expected a GeoJSON Feature");
                }
                analyze(feature);
            }
        }

        private void analyzeFeature(final JsonNode node) {
            final var geometry = node.path(GEOMETRY);
            if (!geometry.isNull()) {
                analyze(geometry);
            }
            final var properties = node.path(PROPERTIES);
            if (!properties.isObject() && !properties.isNull()) {
                throw new IllegalArgumentException("Feature properties must be an object or null");
            }
            for (final var property : properties.properties()) {
                saveKey(property.getKey());
            }
            if (node.path(PROPERTIES).isNull()) {
                saveKey(PROPERTIES);
            }
        }

        private void analyzeCoordinates(final JsonNode coordinates, final int depth) {
            if (!coordinates.isArray()) {
                throw new IllegalArgumentException("Expected coordinate array");
            }
            if (depth == 0) {
                analyzePoint(coordinates);
            } else {
                for (final var child : coordinates) {
                    analyzeCoordinates(child, depth - 1);
                }
            }
        }

        private void analyzePoint(final JsonNode point) {
            if (dimensions == 0) {
                dimensions = point.size();
            }
            if (point.size() != dimensions || dimensions < 2 || dimensions > 3) {
                throw new IllegalArgumentException(
                        "GeoBuf requires consistent 2D or 3D coordinates");
            }
            for (final var coordinate : point) {
                if (!coordinate.isNumber()
                        || !Double.isFinite(coordinate.asDouble())
                        || Math.abs(coordinate.asDouble() * scale) > 9_007_199_254_740_991d) {
                    throw new IllegalArgumentException(
                            "Coordinate exceeds GeoBuf's interoperable integer range");
                }
            }
        }

        private ByteString collection(final JsonNode node) {
            return serialize(
                    output -> {
                        for (final var feature : node.path(FEATURES)) {
                            output.writeBytes(
                                    Data.FeatureCollection.FEATURES_FIELD_NUMBER, feature(feature));
                        }
                        final var values = new ArrayList<Data.Value>();
                        Data.FeatureCollection.newBuilder()
                                .addAllCustomProperties(properties(node, true, values))
                                .addAllValues(values)
                                .build()
                                .writeTo(output);
                    });
        }

        private ByteString feature(final JsonNode node) {
            final var result = Data.Feature.newBuilder();
            if (!node.path(GEOMETRY).isNull()) {
                result.setGeometry(geometry(node.path(GEOMETRY)));
            }
            final var id = node.path("id");
            if (id.isString()) {
                result.setId(id.asString());
            } else if (id.isIntegralNumber() && id.bigIntegerValue().bitLength() <= 63) {
                result.setIntId(id.asLong());
            } else if (!id.isMissingNode()) {
                throw new IllegalArgumentException(
                        "Feature ID must be a string or signed 64-bit integer");
            }
            final var propertyValues = new ArrayList<Data.Value>();
            result.addAllProperties(properties(node.path(PROPERTIES), false, propertyValues));
            result.addAllValues(propertyValues);
            // Mapbox resets its value table after each properties field. Keep the ordinary and
            // custom groups in wire order instead of letting generated serialization merge them.
            final var customValues = new ArrayList<Data.Value>();
            final var custom = Data.Feature.newBuilder();
            if (node.path(PROPERTIES).isNull()) {
                custom.addAllCustomProperties(List.of(requireNonNull(keys.get(PROPERTIES)), 0));
                customValues.add(Data.Value.getDefaultInstance());
            }
            custom.addAllCustomProperties(properties(node, true, customValues));
            custom.addAllValues(customValues);
            return result.build().toByteString().concat(custom.build().toByteString());
        }

        private Data.Geometry geometry(final JsonNode node) {
            final var result =
                    Data.Geometry.newBuilder()
                            .setType(requireNonNull(GEOMETRY_TYPES.get(type(node))));
            final var coords = node.path(COORDINATES);
            switch (type(node)) {
                case POINT -> {
                    for (final var coordinate : coords) {
                        // Scale decimal coordinates to integers on the requested precision grid.
                        result.addCoords(Math.round(coordinate.asDouble() * scale));
                    }
                }
                case MULTI_POINT, LINE_STRING -> line(coords, false, result);
                case MULTI_LINE_STRING, POLYGON ->
                        multiLine(coords, POLYGON.equals(type(node)), result);
                case MULTI_POLYGON -> multiPolygon(coords, result);
                case GEOMETRY_COLLECTION -> {
                    for (final var child : node.path(GEOMETRIES)) {
                        result.addGeometries(geometry(child));
                    }
                }
                default -> throw new IllegalArgumentException("Unknown GeoJSON geometry");
            }
            final var values = new ArrayList<Data.Value>();
            result.addAllCustomProperties(properties(node, true, values));
            return result.addAllValues(values).build();
        }

        private void multiLine(
                final JsonNode lines, final boolean closed, final Data.Geometry.Builder result) {
            if (lines.size() != 1) {
                for (final var ring : lines) {
                    result.addLengths(ring.size() - (closed ? 1 : 0));
                }
            }
            for (final var ring : lines) {
                line(ring, closed, result);
            }
        }

        private void multiPolygon(final JsonNode polygons, final Data.Geometry.Builder result) {
            // Lengths retain the polygon/ring boundaries within the flat coordinate stream.
            if (polygons.size() != 1 || polygons.path(0).size() != 1) {
                result.addLengths(polygons.size());
                for (final var polygon : polygons) {
                    result.addLengths(polygon.size());
                    for (final var ring : polygon) {
                        result.addLengths(ring.size() - 1);
                    }
                }
            }
            for (final var polygon : polygons) {
                for (final var ring : polygon) {
                    line(ring, true, result);
                }
            }
        }

        private void line(
                final JsonNode line, final boolean closed, final Data.Geometry.Builder result) {
            if (closed && line.size() < 4) {
                throw new IllegalArgumentException(
                        "A polygon ring needs at least four coordinates");
            }
            if (closed) {
                for (int axis = 0; axis < dimensions; axis++) {
                    if (line.path(0).path(axis).asDouble()
                            != line.path(line.size() - 1).path(axis).asDouble()) {
                        throw new IllegalArgumentException("Polygon ring is not closed");
                    }
                }
            }
            // Start each line/ring at zero; subsequent points store differences from their
            // neighbor.
            final var previous = new long[dimensions];
            // The decoder restores the closing point of a polygon ring, so omit that duplicate
            // here.
            for (int point = 0; point < line.size() - (closed ? 1 : 0); point++) {
                for (int axis = 0; axis < dimensions; axis++) {
                    // Scale to integers and store deltas for compact varint encoding.
                    final long value = Math.round(line.path(point).path(axis).asDouble() * scale);
                    result.addCoords(value - previous[axis]);
                    previous[axis] = value;
                }
            }
        }

        private List<Integer> properties(
                final JsonNode node, final boolean custom, final List<Data.Value> values) {
            final var indexes = new ArrayList<Integer>();
            for (final var property : node.properties()) {
                if (custom && special(property.getKey(), type(node))) {
                    continue;
                }
                indexes.add(requireNonNull(keys.get(property.getKey())));
                indexes.add(values.size());
                values.add(value(property.getValue()));
            }
            return indexes;
        }

        private static ByteString serialize(final ProtobufWriter writer) {
            final var bytes = ByteString.newOutput();
            final var output = CodedOutputStream.newInstance(bytes);
            try {
                writer.write(output);
                output.flush();
            } catch (final IOException exception) {
                throw new UncheckedIOException("Failed to encode GeoBuf in memory", exception);
            }
            return bytes.toByteString();
        }

        @FunctionalInterface
        private interface ProtobufWriter {
            void write(CodedOutputStream output) throws IOException;
        }

        private static Data.Value value(final JsonNode node) {
            // Use native protobuf types for simple values; preserve nested objects/arrays as JSON.
            final var value = Data.Value.newBuilder();
            if (node.isString()) {
                value.setStringValue(node.asString());
            } else if (node.isBoolean()) {
                value.setBoolValue(node.asBoolean());
            } else if (node.isIntegralNumber() && node.bigIntegerValue().bitLength() <= 63) {
                final long number = node.asLong();
                if (number >= 0) {
                    value.setPosIntValue(number);
                } else {
                    value.setNegIntValue(-number);
                }
            } else if (node.isIntegralNumber()) {
                value.setJsonValue(node.toString());
            } else if (node.isNumber()) {
                value.setDoubleValue(node.asDouble());
            } else if (!node.isNull()) {
                value.setJsonValue(node.toString());
            }
            return value.build();
        }

        private void saveKey(final String key) {
            keys.computeIfAbsent(key, ignored -> keys.size());
        }

        private static String type(final JsonNode node) {
            return node.path("type").asString();
        }

        private static boolean special(final String key, final String type) {
            return "type".equals(key)
                    || switch (type) {
                        case FEATURE_COLLECTION -> FEATURES.equals(key);
                        case FEATURE -> FEATURE_MEMBERS.contains(key);
                        case GEOMETRY_COLLECTION -> GEOMETRIES.equals(key);
                        default -> COORDINATES.equals(key);
                    };
        }
    }
}
