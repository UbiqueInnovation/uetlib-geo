package ch.uetlib.geo.geobuf;

import static java.util.Objects.requireNonNull;

import ch.uetlib.geo.geobuf.protobuf.GeoBuf;
import ch.uetlib.geo.geojson.models.jts.Feature;
import ch.uetlib.geo.geojson.models.jts.FeatureCollection;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encodes GeoJSON and Java/JTS features using the Mapbox GeoBuf schema, delta-encoded coordinates
 * and shared property keys. Precision is expressed in the input coordinate system; no coordinate
 * transformation is performed.
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
        return new Encoder(precision).encode(GeoBufInputs.fromJson(geoJson));
    }

    public byte[] toGeoBufNative(final Geometry geometry) {
        return new Encoder(precision).encode(GeoBufInputs.fromGeometry(geometry));
    }

    public byte[] toGeoBufNative(final Feature feature) {
        return new Encoder(precision).encode(GeoBufInputs.fromFeature(feature));
    }

    public byte[] toGeoBufNative(final FeatureCollection collection) {
        return new Encoder(precision).encode(GeoBufInputs.fromCollection(collection));
    }

    private static final class Encoder {
        private static final String PROPERTIES = "properties";
        private final int precision;
        private final double scale;
        private final Map<String, Integer> keys = new LinkedHashMap<>();
        private int dimensions;

        Encoder(final int precision) {
            this.precision = precision;
            scale = Math.pow(10, precision);
        }

        byte[] encode(final GeoBufInput input) {
            // First pass: infer dimensions and store each property name once in a shared
            // dictionary.
            analyze(input);
            final var header =
                    GeoBuf.newBuilder()
                            .setPrecision(precision)
                            .setDimensions(dimensions == 0 ? 2 : dimensions)
                            .addAllKeys(keys.keySet())
                            .build();
            // Protocol Buffers packs the message, including signed coordinate deltas, into bytes.
            return serialize(
                            output -> {
                                header.writeTo(output);
                                switch (input) {
                                    case GeoBufInput.Collection collection ->
                                            output.writeBytes(
                                                    GeoBuf.FEATURE_COLLECTION_FIELD_NUMBER,
                                                    collection(collection));
                                    case GeoBufInput.Feature feature ->
                                            output.writeBytes(
                                                    GeoBuf.FEATURE_FIELD_NUMBER, feature(feature));
                                    case GeoBufInput.Shape shape ->
                                            output.writeMessage(
                                                    GeoBuf.GEOMETRY_FIELD_NUMBER, geometry(shape));
                                }
                            })
                    .toByteArray();
        }

        private void analyze(final GeoBufInput input) {
            switch (input) {
                case GeoBufInput.Collection collection ->
                        collection.features().forEach(this::analyze);
                case GeoBufInput.Feature feature -> {
                    final var geometry = feature.geometry();
                    if (geometry != null) {
                        analyze(geometry);
                    }
                    final var properties = feature.properties();
                    if (properties == null) {
                        saveKey(PROPERTIES);
                    } else {
                        properties.keySet().forEach(this::saveKey);
                    }
                }
                case GeoBufInput.Shape shape -> analyzeShape(shape);
            }
            input.foreignMembers().keySet().forEach(this::saveKey);
        }

        private void analyzeShape(final GeoBufInput.Shape shape) {
            if (!shape.children().isEmpty()) {
                shape.children().forEach(this::analyze);
                return;
            }
            if (shape.geometry() instanceof Point point && point.isEmpty()) {
                throw new IllegalArgumentException("GeoBuf requires a nonempty point");
            }
            shape.geometry().apply(this::analyzePosition);
        }

        private void analyzePosition(final Coordinate coordinate) {
            final int axes = Double.isNaN(coordinate.getZ()) ? 2 : 3;
            if (dimensions == 0) {
                dimensions = axes;
            }
            if (dimensions != axes || !Double.isNaN(coordinate.getM())) {
                throw new IllegalArgumentException(
                        "GeoBuf requires consistent 2D or 3D coordinates without measures");
            }
            for (int axis = 0; axis < dimensions; axis++) {
                final double value = coordinate.getOrdinate(axis);
                if (!Double.isFinite(value) || Math.abs(value * scale) > 9_007_199_254_740_991d) {
                    throw new IllegalArgumentException(
                            "Coordinate exceeds GeoBuf's interoperable integer range");
                }
            }
        }

        private ByteString collection(final GeoBufInput.Collection collection) {
            return serialize(output -> {
                for (final var feature : collection.features()) {
                    output.writeBytes(
                            GeoBuf.FeatureCollection.FEATURES_FIELD_NUMBER, feature(feature));
                }
                final var values = new ArrayList<GeoBuf.Value>();
                GeoBuf.FeatureCollection.newBuilder()
                        .addAllCustomProperties(properties(collection.foreignMembers(), values))
                        .addAllValues(values)
                        .build()
                        .writeTo(output);
            });
        }

        private ByteString feature(final GeoBufInput.Feature feature) {
            final var result = GeoBuf.Feature.newBuilder();
            final var geometry = feature.geometry();
            if (geometry != null) {
                result.setGeometry(geometry(geometry));
            }
            if (feature.id() instanceof String text) {
                result.setId(text);
            } else if (feature.id() instanceof Long number) {
                result.setIntId(number);
            }
            final var propertyValues = new ArrayList<GeoBuf.Value>();
            result.addAllProperties(properties(feature.properties(), propertyValues));
            result.addAllValues(propertyValues);
            // Mapbox resets its value table after each properties field. Keep the ordinary and
            // custom groups in wire order instead of letting generated serialization merge them.
            final var customValues = new ArrayList<GeoBuf.Value>();
            final var custom = GeoBuf.Feature.newBuilder();
            if (feature.properties() == null) {
                custom.addAllCustomProperties(List.of(requireNonNull(keys.get(PROPERTIES)), 0));
                customValues.add(GeoBuf.Value.getDefaultInstance());
            }
            custom.addAllCustomProperties(properties(feature.foreignMembers(), customValues));
            custom.addAllValues(customValues);
            return result.build().toByteString().concat(custom.build().toByteString());
        }

        private GeoBuf.Geometry geometry(final GeoBufInput.Shape shape) {
            final var result = GeoBuf.Geometry.newBuilder();
            switch (shape.geometry()) {
                case Point point -> {
                    result.setType(GeoBuf.Geometry.Type.POINT);
                    // A zero origin gives the absolute coordinates used by standalone points.
                    position(point.getCoordinateSequence(), 0, new long[dimensions], result);
                }
                case LineString line -> {
                    result.setType(GeoBuf.Geometry.Type.LINESTRING);
                    line(line.getCoordinateSequence(), false, result);
                }
                case Polygon polygon -> {
                    result.setType(GeoBuf.Geometry.Type.POLYGON);
                    polygon(polygon, polygon.getNumInteriorRing() > 0, result);
                }
                case MultiPoint points -> {
                    result.setType(GeoBuf.Geometry.Type.MULTIPOINT);
                    final var previous = new long[dimensions];
                    for (int index = 0; index < points.getNumGeometries(); index++) {
                        final var point = (Point) points.getGeometryN(index);
                        position(point.getCoordinateSequence(), 0, previous, result);
                    }
                }
                case MultiLineString lines -> {
                    result.setType(GeoBuf.Geometry.Type.MULTILINESTRING);
                    multiLine(lines, result);
                }
                case MultiPolygon polygons -> {
                    result.setType(GeoBuf.Geometry.Type.MULTIPOLYGON);
                    multiPolygon(polygons, result);
                }
                case GeometryCollection _ -> {
                    result.setType(GeoBuf.Geometry.Type.GEOMETRYCOLLECTION);
                    shape.children().forEach(child -> result.addGeometries(geometry(child)));
                }
                default -> throw new IllegalArgumentException("Unsupported JTS geometry");
            }
            final var values = new ArrayList<GeoBuf.Value>();
            result.addAllCustomProperties(properties(shape.foreignMembers(), values));
            return result.addAllValues(values).build();
        }

        private void multiLine(final MultiLineString lines, final GeoBuf.Geometry.Builder result) {
            for (int index = 0; index < lines.getNumGeometries(); index++) {
                final var line = (LineString) lines.getGeometryN(index);
                if (lines.getNumGeometries() != 1) {
                    result.addLengths(line.getNumPoints());
                }
                line(line.getCoordinateSequence(), false, result);
            }
        }

        private void multiPolygon(
                final MultiPolygon polygons, final GeoBuf.Geometry.Builder result) {
            // Lengths retain the polygon/ring boundaries within the flat coordinate stream.
            final boolean includeLengths =
                    polygons.getNumGeometries() != 1
                            || ((Polygon) polygons.getGeometryN(0)).getNumInteriorRing() != 0;
            if (includeLengths) {
                result.addLengths(polygons.getNumGeometries());
            }
            for (int index = 0; index < polygons.getNumGeometries(); index++) {
                final var polygon = (Polygon) polygons.getGeometryN(index);
                if (includeLengths) {
                    result.addLengths(polygon.getNumInteriorRing() + 1);
                }
                polygon(polygon, includeLengths, result);
            }
        }

        private void polygon(
                final Polygon polygon,
                final boolean includeLengths,
                final GeoBuf.Geometry.Builder result) {
            for (int index = 0; index <= polygon.getNumInteriorRing(); index++) {
                final var ring = ring(polygon, index);
                if (includeLengths) {
                    result.addLengths(ring.getNumPoints() - 1);
                }
                line(ring.getCoordinateSequence(), true, result);
            }
        }

        private static LineString ring(final Polygon polygon, final int index) {
            return index == 0 ? polygon.getExteriorRing() : polygon.getInteriorRingN(index - 1);
        }

        private void line(
                final CoordinateSequence line,
                final boolean closed,
                final GeoBuf.Geometry.Builder result) {
            // JTS enforces ring length and XY closure. GeoBuf also omits the closing Z value.
            if (closed
                    && (line.size() == 0
                            || (dimensions == 3 && line.getZ(0) != line.getZ(line.size() - 1)))) {
                throw new IllegalArgumentException(
                        "GeoBuf requires a nonempty ring closed in every dimension");
            }
            // Start each line/ring at zero; subsequent points store differences from their
            // neighbor.
            final var previous = new long[dimensions];
            // The decoder restores the closing point of a polygon ring, so omit that duplicate
            // here.
            for (int index = 0; index < line.size() - (closed ? 1 : 0); index++) {
                position(line, index, previous, result);
            }
        }

        private void position(
                final CoordinateSequence sequence,
                final int index,
                final long[] previous,
                final GeoBuf.Geometry.Builder result) {
            for (int axis = 0; axis < dimensions; axis++) {
                // Scale to integers and store deltas for compact varint encoding.
                final long value = Math.round(sequence.getOrdinate(index, axis) * scale);
                result.addCoords(value - previous[axis]);
                previous[axis] = value;
            }
        }

        private List<Integer> properties(
                final @Nullable Map<String, @Nullable Object> properties,
                final List<GeoBuf.Value> values) {
            final var indexes = new ArrayList<Integer>();
            if (properties != null) {
                properties.forEach((key, value) -> {
                    indexes.add(requireNonNull(keys.get(key)));
                    indexes.add(values.size());
                    values.add(value(value));
                });
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

        private static GeoBuf.Value value(final @Nullable Object value) {
            // Use native protobuf types for simple values; preserve nested objects/arrays as JSON.
            final var result = GeoBuf.Value.newBuilder();
            if (value instanceof String text) {
                result.setStringValue(text);
            } else if (value instanceof Boolean bool) {
                result.setBoolValue(bool);
            } else if (value instanceof Number number && isLongInteger(number)) {
                if (number.longValue() >= 0) {
                    result.setPosIntValue(number.longValue());
                } else {
                    result.setNegIntValue(-number.longValue());
                }
            } else if (value instanceof Number number && !(value instanceof BigInteger)) {
                result.setDoubleValue(number.doubleValue());
            } else if (value != null) {
                result.setJsonValue(GeoBufInputs.jsonValue(value));
            }
            return result.build();
        }

        private static boolean isLongInteger(final Number value) {
            return value instanceof Byte
                    || value instanceof Short
                    || value instanceof Integer
                    || value instanceof Long
                    || (value instanceof BigInteger integer && integer.bitLength() <= 63);
        }

        private void saveKey(final String key) {
            keys.computeIfAbsent(key, _ -> keys.size());
        }
    }
}
