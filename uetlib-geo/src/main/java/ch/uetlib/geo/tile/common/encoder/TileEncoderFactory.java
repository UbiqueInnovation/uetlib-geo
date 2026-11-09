package ch.uetlib.geo.tile.common.encoder;

import ch.uetlib.geo.tile.vector.encoder.GeoJsonVectorTileEncoder;
import ch.uetlib.geo.tile.vector.encoder.JsonVectorTileEncoder;
import ch.uetlib.geo.tile.vector.encoder.PbfVectorTileEncoder;
import ch.uetlib.geo.tile.vector.encoder.PngVectorTileEncoder;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;

public final class TileEncoderFactory {

    private TileEncoderFactory() {}

    public static TileEncoder getEncoder(final VectorTileConfig config) {
        final var format = config.outputFormat();
        return switch (format) {
            case PBF -> new PbfVectorTileEncoder(config);
            case JSON -> new JsonVectorTileEncoder(config);
            case GEO_JSON -> new GeoJsonVectorTileEncoder(config);
            case PNG -> new PngVectorTileEncoder(config);
        };
    }
}
