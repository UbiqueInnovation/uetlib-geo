package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementUtils;
import no.ecc.vectortile.VectorTileEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PbfVectorTileEncoder extends AbstractVectorTileEncoder {

    private final VectorTileEncoder encoder;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PbfVectorTileEncoder(final VectorTileConfig config) {
        super(config);
        this.encoder =
                new VectorTileEncoder(config.getExtent(), config.getClipBuffer(), false, false);
    }

    @Override
    public byte[] encodeVectorTile(final VectorTile tile) {
        for (final var element : tile.elements()) {
            final var geometry = getTileGeometry(element, tile.tileInfo().envelope());
            if (geometry.isEmpty()) {
                logger.warn("Skipping {}, since no tile geometry could be created", element);
            } else {
                final var tags = VectorTileElementUtils.getFinalTags(element);
                encoder.addFeature(element.layer(), tags, geometry.get(), element.id());
            }
        }
        return encoder.encode();
    }
}
