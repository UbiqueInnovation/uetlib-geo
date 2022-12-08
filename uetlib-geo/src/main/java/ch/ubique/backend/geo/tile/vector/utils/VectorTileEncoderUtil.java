package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import no.ecc.vectortile.VectorTileEncoder;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VectorTileEncoderUtil {

    private final Tile tile;
    private final VectorTileConfig config;
    private final VectorTileEncoder encoder;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static BinaryTile encode(Tile tile, VectorTileConfig config) {
        final var encoderUtil = new VectorTileEncoderUtil(tile, config);
        final var bytes = encoderUtil.encode();
        return new BinaryTile(tile.tileInfo(), bytes);
    }

    private VectorTileEncoderUtil(Tile tile, VectorTileConfig config) {
        this.tile = tile;
        this.config = config;
        this.encoder =
                new VectorTileEncoder(config.getExtent(), config.getClipBuffer(), false, false);
    }

    public byte[] encode() {
        for (final var element : tile.elements()) {
            final var geometry = getTileGeometry(element);
            final var tags = VectorTileElementUtils.getFinalTags(element);
            if (geometry.isEmpty()) {
                logger.warn("Skipping {}, since no tile geometry could be created", element);
            } else {
                encoder.addFeature(element.layer(), tags, geometry.get(), element.id());
            }
        }

        return encoder.encode();
    }

    private Optional<Geometry> getTileGeometry(VectorTileElement element) {

        if (!element.showElement(tile.tileInfo().level())) {
            return Optional.empty();
        }

        return validateGeometry(element.geometry())
                .flatMap(this::convertToTileCoordinates)
                .flatMap(this::validateGeometry)
                .map(this::simplifyIfNeeded)
                .map(this::smoothingIfNeeded)
                .flatMap(this::validateGeometry);
    }

    private Optional<Geometry> validateGeometry(@NotNull final Geometry geometry) {
        if (geometry.isValid()) {
            return Optional.of(geometry);
        }
        try {
            return emptyIfGeometryEmpty(JtsUtil.tryToGetValidGeometry(geometry));
        } catch (UnfixableGeometryException e) {
            logger.warn("Geometry is not valid and could not be fixed", e);
            return Optional.empty();
        }
    }

    private Optional<Geometry> convertToTileCoordinates(@NotNull final Geometry geometry) {
        final var tileGeometry =
                TileGeometryUtils.getTileGeometry(geometry, tile.boundingBox(), config);
        if (tileGeometry.isEmpty()) {
            logger.warn("Could not convert geometry to tile coordinates");
        }
        return tileGeometry;
    }

    private Geometry simplifyIfNeeded(@NotNull final Geometry geometry) {
        return config.getSimplifyTolerance()
                .map(tolerance -> SimplifyGeometryUtils.simplify(geometry, tolerance))
                .orElse(geometry);
    }

    private Geometry smoothingIfNeeded(@NotNull final Geometry geometry) {
        return config.getSmoothing()
                .map(alpha -> SimplifyGeometryUtils.smooth(geometry, alpha))
                .orElse(geometry);
    }

    private Optional<Geometry> emptyIfGeometryEmpty(@NotNull final Geometry geometry) {
        if (geometry.isEmpty()) {
            logger.warn("The geometry is empty.");
            return Optional.empty();
        }
        return Optional.of(geometry);
    }
}
