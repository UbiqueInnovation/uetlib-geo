package ch.ubique.backend.geo.tile.vector.mapbox;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.geo.tile.vector.mapbox.decoding.VectorTileDecoder;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.ubique.backend.geo.vectortile.protobuf.Tile;
import ch.ubique.backend.geo.vectortile.protobuf.Tile.Feature;
import ch.ubique.backend.geo.vectortile.protobuf.Tile.Layer;
import ch.ubique.backend.geo.vectortile.protobuf.Tile.Value;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class VectorTileDecoderTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/vector-tiles/ubique/post/poi/7/66/44.pbf",
                "/vector-tiles/ubique/post/poi/7/66/45.pbf",
                "/vector-tiles/ubique/post/poi/7/67/44.pbf",
                "/vector-tiles/ubique/post/poi/7/67/45.pbf",
                "/vector-tiles/swisstopo/base/6/33/22.pbf",
                "/vector-tiles/swisstopo/base/13/4290/2868.pbf",
                "/vector-tiles/swisstopo/relief/13/4290/2868.pbf",
            })
    void decode(final String resourcePath) throws Exception {
        final var input = readBytes(resourcePath);
        final var inputTile = Tile.parseFrom(input);
        assertThat(inputTile.toByteArray()).isEqualTo(input);

        final var decoded = VectorTileDecoder.decode(inputTile);
        assertNotNull(decoded);

        final var encoded = VectorTileEncoder.encode(decoded);
        assertNotNull(encoded);

        assertEquals(encoded.getLayersCount(), inputTile.getLayersCount());
        for (var i = 0; i < encoded.getLayersCount(); i++) {
            final var expectedLayer = inputTile.getLayers(i);
            final var actualLayer = encoded.getLayers(i);
            assertEquals(expectedLayer.getVersion(), actualLayer.getVersion());
            assertEquals(expectedLayer.getExtent(), actualLayer.getExtent());
            assertEquals(expectedLayer.getName(), actualLayer.getName());
            assertEquals(expectedLayer.getFeaturesCount(), actualLayer.getFeaturesCount());
            assertThatIterable(actualLayer.getKeysList()).isSubsetOf(expectedLayer.getKeysList());
            assertThatIterable(actualLayer.getValuesList())
                    .isSubsetOf(expectedLayer.getValuesList());
            for (var j = 0; j < actualLayer.getFeaturesCount(); j++) {
                final var expectedFeature = expectedLayer.getFeatures(j);
                final var actualFeature = actualLayer.getFeatures(j);
                assertEquals(expectedFeature.getId(), actualFeature.getId());
                assertEquals(expectedFeature.getType(), actualFeature.getType());
                assertEquals(expectedFeature.getGeometryList(), actualFeature.getGeometryList());
                assertEquals(expectedFeature.getTagsCount(), actualFeature.getTagsCount());

                final var expectedKeys = keyList(expectedFeature, expectedLayer);
                final var actualKeys = keyList(actualFeature, actualLayer);
                assertThatIterable(actualKeys).containsExactlyInAnyOrderElementsOf(expectedKeys);

                final var expectedValues = valueList(expectedFeature, expectedLayer);
                final var actualValues = valueList(actualFeature, actualLayer);
                assertThatIterable(actualValues)
                        .containsExactlyInAnyOrderElementsOf(expectedValues);

                final var expectedTags = toMap(expectedKeys, expectedValues);
                final var actualTags = toMap(actualKeys, actualValues);
                assertEquals(expectedTags, actualTags);
            }
        }
    }

    private static Map<String, Value> toMap(final List<String> keys, final List<Value> values) {
        return IntStream.range(0, keys.size())
                .boxed()
                .collect(Collectors.toMap(keys::get, values::get));
    }

    private static List<String> keyList(final Feature feature, final Layer layer) {
        return IntStream.range(0, feature.getTagsCount())
                .filter(k -> k % 2 == 0)
                .map(feature::getTags)
                .mapToObj(layer::getKeys)
                .toList();
    }

    private static List<Value> valueList(final Feature feature, final Layer layer) {
        return IntStream.range(0, feature.getTagsCount())
                .filter(k -> k % 2 != 0)
                .map(feature::getTags)
                .mapToObj(layer::getValues)
                .toList();
    }

    private byte[] readBytes(final String resourcePath) throws IOException {
        try (final var inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(Objects.requireNonNull(inputStream));
        }
    }
}
