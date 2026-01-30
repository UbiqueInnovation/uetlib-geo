package ch.ubique.backend.geo.geobuf;

import ch.ubique.backend.core.utils.MapCollectors;
import ch.ubique.backend.geo.geobuf.protobuf.Data;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeature;
import ch.ubique.backend.geo.geojson.models.jts.GenericFeatureCollection;
import ch.ubique.backend.geo.jackson.ObjectMapperJtsUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotNull;

import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GeoBufCreator {
    private static final ObjectMapper objectMapper = ObjectMapperJtsUtils.objectMapperWgs84();
    private static final Logger logger = LoggerFactory.getLogger(GeoBufCreator.class);

    private final int precision;
    private final long scale;

    public GeoBufCreator() {
        this(6);
    }

    public GeoBufCreator(final int precision) {
        this.precision = precision;
        this.scale = (long) Math.pow(10, this.precision);
    }

    /**
     * Infers the number of dimensions from the given FeatureCollection's geometries. May be
     * expensive on large geometries with many points if only two dimensions are used. Defaults to 2
     * dimensions if no coordinate with 3 dimensions is found.
     */
    public <
                    P extends Map<String, Object>,
                    F extends GenericFeature<P, F>,
                    C extends GenericFeatureCollection<F, P, C>>
            @NotNull byte[] toGeoBufNative(@NotNull final C featureCollection) {
        return toGeoBufNative(featureCollection, getDimensions(featureCollection));
    }

    public <
                    P extends Map<String, Object>,
                    F extends GenericFeature<P, F>,
                    C extends GenericFeatureCollection<F, P, C>>
            @NotNull byte[] toGeoBufNative(
                    @NotNull final C featureCollection, final int dimensions) {
        final var geobufBuilder =
                Data.newBuilder().setPrecision(precision).setDimensions(dimensions);
        geobufBuilder.addAllKeys(getAllKeys(featureCollection).distinct().toList());
        final var keys = geobufBuilder.getKeysList().stream().toList();
        final var keysMap =
                IntStream.range(0, keys.size())
                        .mapToObj(i -> Map.entry(keys.get(i), i))
                        .collect(MapCollectors.entriesToMap());
        final var featureCollectionBuilder = geobufBuilder.getFeatureCollectionBuilder();
        featureCollectionBuilder.addAllFeatures(
                featureCollection.getFeatures().stream().map(toGeoBufFeature(keysMap)).toList());
        geobufBuilder.setFeatureCollection(featureCollectionBuilder);
        return geobufBuilder.build().toByteArray();
    }

    private int getDimensions(final GenericFeatureCollection<?, ?, ?> featureCollection) {
        final var anyZ =
                featureCollection.getFeatures().stream()
                        .map(GenericFeature::getGeometry)
                        .flatMap(g -> Arrays.stream((g.getCoordinates())))
                        .anyMatch(c -> c.z != Coordinate.NULL_ORDINATE);
        return anyZ ? 3 : 2;
    }

    private static <
                    P extends Map<String, Object>,
                    F extends GenericFeature<P, F>,
                    C extends GenericFeatureCollection<F, P, C>>
            @NotNull Stream<String> getAllKeys(@NotNull final C featureCollection) {
        return featureCollection.getFeatures().stream().flatMap(GeoBufCreator::getAllKeys);
    }

    private static <P extends Map<String, Object>, F extends GenericFeature<P, F>>
            @NotNull Stream<String> getAllKeys(@NotNull final F feature) {
        return feature.getProperties().entrySet().stream()
                .map(Map.Entry::getKey)
                .filter(Predicate.not(isSpecialKey(feature.getType())));
    }

    private <P extends Map<String, Object>, F extends GenericFeature<P, F>>
            @NotNull Function<F, Data.Feature> toGeoBufFeature(final Map<String, Integer> keys) {
        return feature -> {
            final var featureBuilder = Data.Feature.newBuilder();
            featureBuilder.setId(feature.getId());
            final var valuesToIdx =
                    feature.getProperties().entrySet().stream()
                            .map(addValue(featureBuilder))
                            .collect(MapCollectors.entriesToMap());
            featureBuilder
                    .addAllProperties(
                            feature.getProperties().entrySet().stream()
                                    .flatMap(
                                            entry ->
                                                    Stream.of(
                                                            keys.get(entry.getKey()),
                                                            valuesToIdx.get(entry.getValue())))
                                    .toList())
                    .setGeometry(jtsToPbfGeometry(feature.getGeometry()));
            return featureBuilder.build();
        };
    }

    private static @NotNull Function<Map.Entry<String, Object>, Map.Entry<Object, Integer>>
            addValue(final Data.Feature.Builder builder) {
        return entry -> {
            final var value = objectToPbfValue(entry.getValue());
            final var existingIndexMaybe = builder.getValuesList().lastIndexOf(value);
            if (existingIndexMaybe != -1) {
                return Map.entry(entry.getValue(), existingIndexMaybe);
            }
            builder.addValues(value);
            return Map.entry(entry.getValue(), builder.getValuesList().lastIndexOf(value));
        };
    }

    private Data.Geometry jtsToPbfGeometry(final Geometry geometry) {
        final var builder = Data.Geometry.newBuilder();
        return switch (geometry) {
            case final Point point ->
                    builder.setType(Data.Geometry.Type.POINT)
                            .addCoords(deltaEncode(point.getCoordinate().x))
                            .addCoords(deltaEncode(point.getCoordinate().y))
                            .build();
            case final LineString lineString -> {
                final var deltaEncoded = deltaEncodeLineString(lineString);
                yield builder.setType(Data.Geometry.Type.LINESTRING)
                        .addLengths(deltaEncoded.size() / 2)
                        .addAllCoords(deltaEncoded)
                        .build();
            }
            case final Polygon polygon -> {
                final var deltaEncoded = deltaEncodePolygon(polygon);
                yield builder.setType(Data.Geometry.Type.POLYGON)
                        .addAllLengths(
                                deltaEncoded.stream().map(List::size).map(i -> i / 2).toList())
                        .addAllCoords(deltaEncoded.stream().flatMap(List::stream).toList())
                        .build();
            }
            case final MultiPolygon multiPolygon -> {
                final var deltaEncoded = deltaEncodeMultiPolygon(multiPolygon);
                yield buildGeometry(builder, deltaEncoded, Data.Geometry.Type.MULTIPOLYGON);
            }
            case final GeometryCollection geometryCollection -> {
                final var deltaEncoded = deltaEncodeGeometryCollection(geometryCollection);
                yield buildGeometry(builder, deltaEncoded, Data.Geometry.Type.GEOMETRYCOLLECTION);
            }
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported geometry type: " + geometry.getGeometryType());
        };
    }

    private static Data.Geometry buildGeometry(
            final Data.Geometry.Builder builder,
            final List<List<List<Long>>> deltaEncoded,
            final Data.Geometry.Type type) {
        return builder.setType(type)
                .addAllLengths(getLengths3DList(deltaEncoded))
                .addAllCoords(
                        deltaEncoded.stream().flatMap(List::stream).flatMap(List::stream).toList())
                .build();
    }

    private List<List<List<Long>>> deltaEncodeMultiPolygon(final MultiPolygon multiPolygon) {
        return IntStream.range(0, multiPolygon.getNumGeometries())
                .mapToObj(multiPolygon::getGeometryN)
                .map(
                        geom -> {
                            if (!(geom instanceof final Polygon p)) {
                                throw new IllegalArgumentException(
                                        "All Children of MultiPolygon should be Polygon.");
                            }
                            return deltaEncodePolygon(p);
                        })
                .toList();
    }

    private List<List<List<Long>>> deltaEncodeGeometryCollection(
            final GeometryCollection geometryCollection) {
        return IntStream.range(0, geometryCollection.getNumGeometries())
                .mapToObj(geometryCollection::getGeometryN)
                .map(
                        geom ->
                                switch (geom) {
                                    case final Polygon polygon -> deltaEncodePolygon(polygon);
                                    case final LineString lineString ->
                                            List.of(
                                                    deltaEncodeLineString(lineString).stream()
                                                            .toList());
                                    // case final MultiPolygon multiPolygon ->
                                    // deltaEncodeMultiPolygon(multiPolygon).stream().toList();
                                    case final Point point ->
                                            List.of(
                                                    List.of(
                                                            deltaEncode(point.getCoordinate().x),
                                                            deltaEncode(point.getCoordinate().y)));
                                    default ->
                                            throw new IllegalArgumentException(
                                                    "Unsupported geometry type: " + geom);
                                })
                .toList();
    }

    private static List<Integer> getLengths3DList(final List<List<List<Long>>> coords) {
        return Stream.concat(
                        Stream.of(coords.size()),
                        coords.stream().flatMap(GeoBufCreator::getLengths2DList))
                .toList();
    }

    private static Stream<Integer> getLengths2DList(final List<List<Long>> coords) {
        return Stream.concat(
                Stream.of(coords.size()), coords.stream().map(List::size).map(i -> i / 2));
    }

    private List<List<Long>> deltaEncodePolygon(final Polygon polygon) {
        return Stream.concat(
                        Stream.of(deltaEncode(polygon.getExteriorRing().getCoordinates())),
                        IntStream.range(0, polygon.getNumInteriorRing())
                                .mapToObj(polygon::getInteriorRingN)
                                .map(LinearRing::getCoordinates)
                                .map(this::deltaEncode))
                .toList();
    }

    private List<Long> deltaEncodeLineString(final LineString lineString) {
        return deltaEncode(lineString.getCoordinates());
    }

    private List<Long> deltaEncode(final Coordinate[] coordinates) {
        final var deltaEncoded = new ArrayList<Long>();
        long lastX = 0;
        long lastY = 0;

        for (final var coord : coordinates) {
            final var x = deltaEncode(coord.x);
            final var y = deltaEncode(coord.y);
            final var dx = x - lastX;
            final var dy = y - lastY;
            deltaEncoded.add(dx);
            deltaEncoded.add(dy);
            lastX = x;
            lastY = y;
        }

        return deltaEncoded;
    }

    private Long deltaEncode(final double coordinateValue) {
        return Math.round(coordinateValue * scale);
    }

    private static Data.Value objectToPbfValue(final Object value) {
        try {
            return Data.Value.newBuilder()
                    .setJsonValue(objectMapper.writeValueAsString(value))
                    .build();
        } catch (final JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Predicate<String> isSpecialKey(final String type) {
        return key -> {
            if (Objects.equals(key, "type")) {
                return true;
            }
            return switch (type) {
                case "FeatureCollection" -> Objects.equals(key, "features");
                case "Feature" ->
                        Objects.equals(key, "id")
                                || Objects.equals(key, "properties")
                                || Objects.equals(key, "geometry");
                case "GeometryCollection" -> Objects.equals(key, "geometries");
                default -> Objects.equals(key, "coordinates");
            };
        };
    }

    public static boolean isGeoBufNodeAvailable() {
        try {
            final var process = Runtime.getRuntime().exec(new String[] {"which", "geobuf2json"});
            final var result = process.getInputStream().readAllBytes();
            return result.length > 0;
        } catch (final IOException e) {
            return false;
        }
    }
}
