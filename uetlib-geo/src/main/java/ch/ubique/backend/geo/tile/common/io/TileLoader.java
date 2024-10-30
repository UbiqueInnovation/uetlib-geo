package ch.ubique.backend.geo.tile.common.io;

import ch.ubique.backend.core.utils.InputStreamUtils;
import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;

public class TileLoader {

    private final String baseUrl;
    private final String contentEncoding;

    public TileLoader(final String baseUrl, final String contentEncoding) {
        this.baseUrl = baseUrl;
        this.contentEncoding = contentEncoding;
    }

    public BinaryTile loadPbfTile(final TileInfo tileInfo) throws IOException {
        final var url = URI.create(baseUrl).resolve(tileInfo.path()).toURL();
        try (final var inputStream = url.openStream()) {
            try (final var uncompressed =
                    InputStreamUtils.uncompress(inputStream, contentEncoding, url.getPath())) {
                return new BinaryTile(
                        tileInfo, TileOutputFormat.PBF, IOUtils.toByteArray(uncompressed));
            }
        }
    }
}
