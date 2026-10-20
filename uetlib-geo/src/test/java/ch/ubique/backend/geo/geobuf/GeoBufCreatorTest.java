package ch.ubique.backend.geo.geobuf;

import static org.junit.jupiter.api.Assertions.*;

import static java.util.Objects.requireNonNull;

import ch.ubique.backend.geo.geobuf.protobuf.GeoBuf;
import ch.ubique.backend.geo.geojson.models.jts.Feature;
import ch.ubique.backend.geo.geojson.models.jts.FeatureCollection;
import ch.ubique.backend.geo.jackson.JsonMapperJtsUtils;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class GeoBufCreatorTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @ParameterizedTest
    @CsvSource({"all-geometries, 2", "three-dimensional, 3", "foreign-members, 2"})
    void matchesMapboxReferenceEncoder(final String name, final int dimensions) throws IOException {
        // Reference outputs from mapbox/geobuf 4.0.0 and pbf 5.1.2.
        final var input = mapper.readTree(resource(name + ".json"));
        final var actual = GeoBuf.parseFrom(new GeoBufCreator().toGeoBufNative(input));
        // The reference omits default precision/dimensions; our encoder writes them explicitly.
        final var expected =
                GeoBuf.parseFrom(resource(name + ".geobuf")).toBuilder()
                        .setPrecision(6)
                        .setDimensions(dimensions)
                        .build();
        assertEquals(expected, actual);
    }

    @Test
    void preservesNullGeometryAndNullProperties() throws IOException {
        final var input =
                mapper.readTree(
                        """
                        {"type":"Feature","id":1,"geometry":null,"properties":null}
                        """);
        final var result = GeoBuf.parseFrom(new GeoBufCreator().toGeoBufNative(input));
        assertFalse(result.getFeature().hasGeometry());
        assertEquals(1, result.getFeature().getIntId());
        assertEquals("properties", result.getKeys(0));
        assertEquals(List.of(0, 0), result.getFeature().getCustomPropertiesList());
        assertEquals(GeoBuf.Value.getDefaultInstance(), result.getFeature().getValues(0));
    }

    @Test
    void rejectsMixedDimensionsAndUnclosedRings() {
        final var mixed =
                mapper.readTree(
                        """
                        {"type":"LineString","coordinates":[[1,2],[3,4,5]]}
                        """);
        final var open =
                mapper.readTree(
                        """
                        {"type":"Polygon","coordinates":[[[1,2],[3,4],[5,6],[7,8]]]}
                        """);
        final var encoder = new GeoBufCreator();
        assertThrows(IllegalArgumentException.class, () -> encoder.toGeoBufNative(mixed));
        assertThrows(IllegalArgumentException.class, () -> encoder.toGeoBufNative(open));
        assertThrows(IllegalArgumentException.class, () -> new GeoBufCreator(-1));
        assertThrows(IllegalArgumentException.class, () -> new GeoBufCreator(10));
    }

    @Test
    void encodingDoesNotReuseKeysOrDimensionsFromPreviousCalls() throws IOException {
        final var creator = new GeoBufCreator();
        creator.toGeoBufNative(mapper.readTree(resource("three-dimensional.json")));
        final var input =
                mapper.readTree(
                        """
                        {"type":"Feature","properties":{"other":true},"geometry":{"type":"Point","coordinates":[1,2]}}
                        """);
        assertArrayEquals(new GeoBufCreator().toGeoBufNative(input), creator.toGeoBufNative(input));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                """
                {"type":"Point","coordinates":[[1,2],[3,4]]}
                """,
                """
                {"type":"LineString","coordinates":[1,2]}
                """,
                """
                {"type":"MultiPolygon","coordinates":[[[1,2],[3,4]]]}
                """,
                """
                {"type":"FeatureCollection","features":{}}
                """,
                """
                {"type":"FeatureCollection","features":[{"type":"Point","coordinates":[1,2]}]}
                """,
                """
                {"type":"GeometryCollection","geometries":{}}
                """,
                """
                {"type":"Feature","geometry":null,"properties":[]}
                """,
                """
                {"type":"Point","coordinates":[1,2,3,4]}
                """,
                """
                {"type":"Polygon","coordinates":[[[0,0,1],[1,0,1],[0,1,1],[0,0,2]]]}
                """
            })
    void rejectsMalformedGeoJsonStructure(final String input) {
        final var json = mapper.readTree(input);
        final var encoder = new GeoBufCreator();
        assertThrows(IllegalArgumentException.class, () -> encoder.toGeoBufNative(json));
    }

    @Test
    void acceptsRingsClosedWithOppositeSignedZeros() throws IOException {
        final var input =
                mapper.readTree(
                        """
                        {"type":"Polygon","coordinates":[[[0.0,0.0],[1,0],[0,1],[-0.0,0.0]]]}
                        """);
        final var geometry =
                GeoBuf.parseFrom(new GeoBufCreator().toGeoBufNative(input)).getGeometry();
        assertEquals(6, geometry.getCoordsCount());
    }

    @Test
    void preservesReferenceWireOrderForForeignFeatureMembers() throws IOException {
        final var input = mapper.readTree(resource("foreign-members.json"));
        final var actual = new GeoBufCreator().toGeoBufNative(input);
        // Parsing and reserializing a generated Feature would reorder its two value groups.
        // Compare the nested bytes too, so a protobuf-message equality check cannot hide that.
        assertArrayEquals(
                featureCollectionPayload(resource("foreign-members.geobuf")),
                featureCollectionPayload(actual));
    }

    @ParameterizedTest
    @ValueSource(strings = {"all-geometries", "three-dimensional"})
    void encodesJtsGeometriesWithoutJsonSerialization(final String name) throws IOException {
        final var input = mapper.readTree(resource(name + ".json"));
        final var features = input.has("features") ? input.path("features") : List.of(input);
        final var jtsMapper = JsonMapperJtsUtils.jsonMapperLv95();
        final var creator = new GeoBufCreator();
        for (final var feature : features) {
            final var json = feature.path("geometry");
            final var geometry = jtsMapper.treeToValue(json, Geometry.class);
            assertArrayEquals(creator.toGeoBufNative(json), creator.toGeoBufNative(geometry));
        }
    }

    @Test
    void encodesExistingFeatureAndFeatureCollectionModels() {
        final var geometry =
                new GeometryFactory().createPoint(new CoordinateXY(2600000.1, 1200000));
        final var feature =
                new Feature()
                        .setId("pasture-1")
                        .setGeometry(geometry)
                        .setProperties(Map.of("name", "pasture"));
        final var json =
                mapper.readTree(
                        """
                {"type":"Feature","id":"pasture-1","properties":{"name":"pasture"},
                "geometry":{"type":"Point","coordinates":[2600000.1,1200000]}}
                """);
        final var creator = new GeoBufCreator();
        assertArrayEquals(creator.toGeoBufNative(json), creator.toGeoBufNative(feature));
        final var collection = new FeatureCollection().setFeatures(List.of(feature, new Feature()));
        final var collectionJson = mapper.createObjectNode().put("type", "FeatureCollection");
        collectionJson
                .putArray("features")
                .add(json)
                .addObject()
                .put("type", "Feature")
                .putNull("properties")
                .putNull("geometry");
        assertArrayEquals(
                creator.toGeoBufNative(collectionJson), creator.toGeoBufNative(collection));
    }

    @Test
    void preservesForeignMembersOnNestedGeometryCollections() throws IOException {
        final var input =
                mapper.readTree(
                        """
                {"type":"GeometryCollection","label":"outer","geometries":[
                  {"type":"Point","coordinates":[1,2],"label":"point"},
                  {"type":"GeometryCollection","label":"inner","geometries":[
                    {"type":"LineString","coordinates":[[1,2],[3,4]],"label":"line"}
                  ]}
                ]}
                """);
        final var result = GeoBuf.parseFrom(new GeoBufCreator().toGeoBufNative(input));
        final var outer = result.getGeometry();
        final var inner = outer.getGeometries(1);
        assertEquals(List.of("label"), result.getKeysList());
        for (final var geometry :
                List.of(outer, outer.getGeometries(0), inner, inner.getGeometries(0))) {
            assertEquals(List.of(0, 0), geometry.getCustomPropertiesList());
        }
        assertEquals("outer", outer.getValues(0).getStringValue());
        assertEquals("point", outer.getGeometries(0).getValues(0).getStringValue());
        assertEquals("inner", inner.getValues(0).getStringValue());
        assertEquals("line", inner.getGeometries(0).getValues(0).getStringValue());
    }

    private static byte[] featureCollectionPayload(final byte[] data) throws IOException {
        final var input = CodedInputStream.newInstance(data);
        while (!input.isAtEnd()) {
            final int tag = input.readTag();
            if (WireFormat.getTagFieldNumber(tag) == GeoBuf.FEATURE_COLLECTION_FIELD_NUMBER) {
                return input.readByteArray();
            }
            input.skipField(tag);
        }
        throw new IllegalArgumentException("Missing feature collection");
    }

    private byte[] resource(final String name) throws IOException {
        try (final var stream = requireNonNull(getClass().getResourceAsStream("/geobuf/" + name))) {
            return stream.readAllBytes();
        }
    }
}
