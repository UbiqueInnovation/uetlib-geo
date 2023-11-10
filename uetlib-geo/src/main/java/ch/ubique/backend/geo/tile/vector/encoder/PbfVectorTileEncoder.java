package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementUtils;

import no.ecc.vectortile.VectorTileEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PbfVectorTileEncoder extends AbstractVectorTileEncoder {

    private static final Logger logger = LoggerFactory.getLogger(PbfVectorTileEncoder.class);

    private final VectorTileEncoder encoder;

    public PbfVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        this.encoder = new VectorTileEncoder(config.extent(), config.clipBuffer(), false, false);
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
