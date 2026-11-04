package ch.uetlib.geo.tile.vector.encoder;

import ch.uetlib.geo.tile.vector.mapbox.encoding.VectorTileEncoder;
import ch.uetlib.geo.tile.vector.models.VectorTile;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;
import ch.uetlib.geo.tile.vector.utils.VectorTileElementUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PbfVectorTileEncoder extends AbstractVectorTileEncoder {

    private static final Logger logger = LoggerFactory.getLogger(PbfVectorTileEncoder.class);

    private final VectorTileEncoder encoder;

    public PbfVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        this.encoder = new VectorTileEncoder(config.extent());
    }

    @Override
    public byte[] encodeVectorTile(final VectorTile tile) {
        if (tile.elements().isEmpty()) {
            return new byte[0];
        }
        for (final var element : tile.elements()) {
            final var geometry = getTileGeometry(element, tile.tileInfo().envelope());
            if (geometry.isEmpty()) {
                logger.atLevel(getConfig().emptyGeometryLogLevel())
                        .log("Skipping {}, since no tile geometry could be created", element);
            } else {
                final var tags = VectorTileElementUtils.getFinalTags(element, getConfig());
                encoder.addFeature(element.layer(), tags, geometry.get(), element.id());
            }
        }
        return encoder.encode();
    }
}
