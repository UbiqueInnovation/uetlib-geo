package ch.ubique.backend.geo.tile.common.io;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJson;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

class TileLoaderTest {

    static Collection<TileLoader> createTileLoaders() throws IOException {
        final var tileUrl =
                "https://vectortiles.geo.admin.ch/tiles/ch.swisstopo.relief.vt/v1.0.0/{z}/{x}/{y}.pbf";
        final var tileJsonUrl =
                "https://vectortiles.geo.admin.ch/tiles/ch.swisstopo.base.vt/v1.0.0/tiles.json";

        final var tileJson = getTestTileJson();
        return List.of(
                TileLoader.fromTileJsonUrl(tileJsonUrl),
                TileLoader.fromTileJsonUrl(URI.create(tileJsonUrl).toURL()),
                TileLoader.fromTileJson(tileJson),
                TileLoader.fromUrl(tileUrl));
    }

    @ParameterizedTest
    @MethodSource("createTileLoaders")
    void fromTileJsonUrl(final TileLoader tileLoader) throws IOException {
        assertNotNull(tileLoader);
        final var bytes = tileLoader.loadZXY(6, 33, 22);
        assertThat(bytes).isNotEmpty();
    }

    private static TileJson getTestTileJson() throws IOException {
        final var objectMapper = ObjectMapperUtils.objectMapper();
        try (final var inputStream =
                TileLoaderTest.class.getResourceAsStream("/tilejson/ch.swisstopo.relief.vt.json")) {
            return objectMapper.readValue(inputStream, TileJson.class);
        }
    }
}
