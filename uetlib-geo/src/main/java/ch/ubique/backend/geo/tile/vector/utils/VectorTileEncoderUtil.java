package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import no.ecc.vectortile.VectorTileEncoder;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VectorTileEncoderUtil<T extends VectorTileElement> {

    private final Tile<T> tile;
    private final VectorTileConfig<T> config;
    private final VectorTileEncoder encoder;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static <S extends VectorTileElement> BinaryTile encode(Tile<S> tile, VectorTileConfig<S> config) {
        final var encoderUtil = new VectorTileEncoderUtil<>(tile, config);
        final var bytes = encoderUtil.encode();
        return new BinaryTile(tile.tileInfo(), bytes);
    }

    private VectorTileEncoderUtil(Tile<T> tile, VectorTileConfig<T> config) {
        this.tile = tile;
        this.config = config;
        this.encoder = new VectorTileEncoder(config.getExtent(), config.getClipBuffer(), false, false);
    }

    public byte[] encode()  {
        for (final var element : tile.elements()) {
            encodeElement(element);
        }

        return encoder.encode();
    }

    private void encodeElement(T element) {

        if (!element.showElement(tile.tileInfo().level())) {
            return;
        }

        final var boundingBox = tile.boundingBox();
        final var layer = element.layer();
        final long id = element.id();
        final var tags = VectorTileElementUtils.getFinalTags(element);

        final Geometry geometry;
        try {
            geometry = JtsUtil.tryToGetValidGeometry(element.geometry());
        } catch (UnfixableGeometryException e) {
            logger.warn("Element geometry for {} not valid and could not be fixed", tags, e);
            return;
        }

        if (geometry.isEmpty()) {
            logger.warn("Geometry empty, going to skip it.");
            return;
        }

        final var tileGeometryMaybe =
                TileGeometryUtils.getTileGeometry(
                        geometry, boundingBox, config.getExtent(), config.getYAxisDirection());
        if (tileGeometryMaybe.isEmpty()) {
            logger.warn("Could not get tile geometry for {}", tags);
            return;
        }

        final Geometry tileGeometry;
        try {
            tileGeometry = JtsUtil.tryToGetValidGeometry(tileGeometryMaybe.get());
        } catch (UnfixableGeometryException e) {
            logger.warn("Tile geometry for {} not valid and could not be fixed", tags, e);
            return;
        }

        if (tileGeometry.isEmpty()) {
            logger.warn("Tile geometry empty, going to skip it.");
            return;
        }

        final var outputGeometry =
                config.simplify()
                        ? SimplifyGeometryUtils.simplify(tileGeometry, config.simplifyTolerance())
                        : tileGeometry;

        encoder.addFeature(layer, tags, outputGeometry, id);
    }
}
