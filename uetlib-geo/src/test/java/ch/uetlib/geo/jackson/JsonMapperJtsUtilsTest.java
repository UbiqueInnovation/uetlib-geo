package ch.uetlib.geo.jackson;

import static ch.uetlib.geo.jackson.JsonMapperJtsUtils.jsonMapperLv95;

import static org.junit.jupiter.api.Assertions.*;

import ch.uetlib.geo.geojson.models.jts.FeatureCollection;

import com.google.common.io.Resources;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

class ObjectMapperJtsUtilsTest {

    private static final ObjectMapper OBJECT_MAPPER = jsonMapperLv95();

    private static String readResourceFile(final String path) throws IOException {
        final var url = Resources.getResource(path);
        return Resources.toString(url, StandardCharsets.UTF_8);
    }

    private static Stream<Arguments> provideLv95Parameters() {
        return Stream.of(
                Arguments.of("furka", 4733, new Coordinate(2690265.32, 1153439.526)),
                Arguments.of(
                        "alp_la_schera", 39, new Coordinate(2810567.221, 1170681.166, 1713.932)),
                Arguments.of(
                        "large_example", 6471, new Coordinate(2653787.519, 1139371.698, 1112.449)));
    }

    @ParameterizedTest
    @MethodSource("provideLv95Parameters")
    void objectMapperLv95(
            final String path, final int numFeatures, final Coordinate firstCoordinate)
            throws IOException {
        final var geoJson = readResourceFile(path + ".geojson");
        final var featureCollection = OBJECT_MAPPER.readValue(geoJson, FeatureCollection.class);
        assertEquals(numFeatures, featureCollection.getFeatures().size());
        final var firstFeature = featureCollection.getFeatures().stream().findFirst().orElseThrow();
        assertEquals(firstCoordinate, firstFeature.getGeometry().getGeometryN(0).getCoordinate());
    }
}
