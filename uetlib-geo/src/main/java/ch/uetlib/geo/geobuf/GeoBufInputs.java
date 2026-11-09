package ch.uetlib.geo.geobuf;

import ch.uetlib.geo.geojson.models.jts.Feature;
import ch.uetlib.geo.geojson.models.jts.FeatureCollection;
import ch.uetlib.geo.jackson.JsonMapperJtsUtils;

import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/** Adapts JSON and the existing Java feature models to typed encoder inputs. */
final class GeoBufInputs {
    private static final String TYPE = "type";
    private static final String COORDINATES = "coordinates";
    private static final String GEOMETRIES = "geometries";
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";
    private static final Set<String> FEATURE_MEMBERS = Set.of(TYPE, "id", "geometry", "properties");
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final JsonMapper MAPPER;

    static {
        final var builder = JsonMapper.builder();
        JsonMapperJtsUtils.configureJts(builder, GEOMETRY_FACTORY);
        MAPPER = builder.build();
    }

    private GeoBufInputs() {}

    static GeoBufInput fromJson(final JsonNode json) {
        return switch (json.path(TYPE).asString()) {
            case "FeatureCollection" ->
                    new GeoBufInput.Collection(
                            array(json.path("features"))
                                    .valueStream()
                                    .map(GeoBufInputs::feature)
                                    .toList(),
                            members(json, Set.of(TYPE, "features")));
            case "Feature" -> feature(json);
            default -> geometry(json);
        };
    }

    private static GeoBufInput.Feature feature(final JsonNode json) {
        if (!"Feature".equals(json.path(TYPE).asString())) {
            throw new IllegalArgumentException("Expected a GeoJSON Feature");
        }
        final var geometry = json.path("geometry");
        final var properties = json.path("properties");
        if (!properties.isNull()) {
            requireObject(properties);
        }
        final var id = json.path("id");
        final Object featureId;
        if (id.isMissingNode()) {
            featureId = null;
        } else if (id.isString()) {
            featureId = id.asString();
        } else if (id.isIntegralNumber() && id.bigIntegerValue().bitLength() <= 63) {
            featureId = id.asLong();
        } else {
            throw new IllegalArgumentException(
                    "Feature ID must be a string or signed 64-bit integer");
        }
        return new GeoBufInput.Feature(
                featureId,
                geometry.isNull() ? null : geometry(geometry),
                properties.isNull() ? null : members(properties, Set.of()),
                members(json, FEATURE_MEMBERS));
    }

    private static GeoBufInput.Shape geometry(final JsonNode json) {
        requireObject(json);
        if (GEOMETRY_COLLECTION.equals(json.path(TYPE).asString())) {
            final var children =
                    array(json.path(GEOMETRIES)).valueStream().map(GeoBufInputs::geometry).toList();
            final var geometry =
                    GEOMETRY_FACTORY.createGeometryCollection(
                            children.stream()
                                    .map(GeoBufInput.Shape::geometry)
                                    .toArray(Geometry[]::new));
            return new GeoBufInput.Shape(
                    geometry, children, members(json, Set.of(TYPE, GEOMETRIES)));
        }
        validateCoordinates(json.path(COORDINATES));
        try {
            return new GeoBufInput.Shape(
                    MAPPER.treeToValue(json, Geometry.class),
                    List.of(),
                    members(json, Set.of(TYPE, COORDINATES)));
        } catch (final JacksonException exception) {
            throw new IllegalArgumentException("Invalid GeoJSON geometry", exception);
        }
    }

    static GeoBufInput.Feature fromFeature(final Feature feature) {
        final var geometry = feature.getGeometry();
        final var properties = feature.getProperties();
        return new GeoBufInput.Feature(
                feature.getId(),
                geometry == null ? null : fromGeometry(geometry),
                properties == null ? null : Collections.unmodifiableMap(properties),
                Map.of());
    }

    static GeoBufInput.Collection fromCollection(final FeatureCollection collection) {
        return new GeoBufInput.Collection(
                collection.getFeatures().stream().map(GeoBufInputs::fromFeature).toList(),
                Map.of());
    }

    static GeoBufInput.Shape fromGeometry(final Geometry geometry) {
        final var children =
                GEOMETRY_COLLECTION.equals(geometry.getGeometryType())
                        ? IntStream.range(0, geometry.getNumGeometries())
                                .mapToObj(index -> fromGeometry(geometry.getGeometryN(index)))
                                .toList()
                        : List.<GeoBufInput.Shape>of();
        return new GeoBufInput.Shape(geometry, children, Map.of());
    }

    private static Map<String, @Nullable Object> members(
            final JsonNode json, final Set<String> excluded) {
        final var result = new LinkedHashMap<String, @Nullable Object>();
        json.properties().stream()
                .filter(entry -> !excluded.contains(entry.getKey()))
                .forEach(
                        entry ->
                                result.put(
                                        entry.getKey(),
                                        MAPPER.convertValue(entry.getValue(), Object.class)));
        return result;
    }

    private static void validateCoordinates(final JsonNode coordinates) {
        array(coordinates);
        if (coordinates.isEmpty()) {
            return;
        }
        if (coordinates.path(0).isArray()) {
            coordinates.valueStream().forEach(GeoBufInputs::validateCoordinates);
        } else if (coordinates.size() < 2
                || coordinates.size() > 3
                || coordinates
                        .valueStream()
                        .anyMatch(
                                value -> !value.isNumber() || !Double.isFinite(value.asDouble()))) {
            // The JTS reader accepts extra ordinates; do not silently discard a fourth dimension.
            throw new IllegalArgumentException("Expected a finite 2D or 3D position");
        }
    }

    private static JsonNode array(final JsonNode json) {
        if (!json.isArray()) {
            throw new IllegalArgumentException("Expected a GeoJSON array");
        }
        return json;
    }

    private static void requireObject(final JsonNode json) {
        if (!json.isObject()) {
            throw new IllegalArgumentException("Expected a GeoJSON object");
        }
    }

    static String jsonValue(final Object value) {
        return MAPPER.writeValueAsString(value);
    }
}
