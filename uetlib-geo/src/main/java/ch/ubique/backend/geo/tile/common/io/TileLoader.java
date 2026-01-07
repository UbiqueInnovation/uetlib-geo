package ch.ubique.backend.geo.tile.common.io;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.core.utils.InputStreamUtils;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJson;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;

public class TileLoader {

    private final List<String> tileUrls;

    private TileLoader(final List<String> tileUrls) {
        this.tileUrls = tileUrls;
    }

    public static TileLoader fromTileJsonUrl(final String tileJsonUrl) throws IOException {
        return fromTileJsonUrl(URI.create(tileJsonUrl).toURL());
    }

    public static TileLoader fromTileJsonUrl(final URL tileJsonUrl) throws IOException {
        return fromTileJson(loadTileJson(tileJsonUrl));
    }

    public static TileJson loadTileJson(final URL tileJsonUrl) throws IOException {
        final var objectMapper = ObjectMapperUtils.jsonMapper();
        try (final var inputStream = getInputStream(tileJsonUrl)) {
            return objectMapper.readValue(inputStream, TileJson.class);
        }
    }

    public static TileLoader fromTileJson(final TileJson tileJson) {
        return new TileLoader(tileJson.tiles());
    }

    public static TileLoader fromUrl(final String url) {
        return new TileLoader(List.of(url));
    }

    public BinaryTile loadPbfTile(final TileInfo tileInfo) throws IOException {
        return new BinaryTile(
                tileInfo,
                TileOutputFormat.PBF,
                loadZXY(tileInfo.level(), tileInfo.x(), tileInfo.y()));
    }

    public byte[] loadZXY(final int z, final int x, final int y) throws IOException {
        final var path =
                tileUrls.getFirst()
                        .replace("{x}", String.valueOf(x))
                        .replace("{y}", String.valueOf(y))
                        .replace("{z}", String.valueOf(z));

        final var url = URI.create(path).toURL();
        try (final var inputStream = getInputStream(url)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private static InputStream getInputStream(final URL url) throws IOException {
        final var connection = url.openConnection();
        final var contentEncoding = connection.getContentEncoding();
        final var inputStream = connection.getInputStream();
        return InputStreamUtils.uncompress(inputStream, contentEncoding);
    }
}
