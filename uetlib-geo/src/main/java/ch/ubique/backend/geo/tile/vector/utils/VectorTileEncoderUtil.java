package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.Function;
import no.ecc.vectortile.VectorTileEncoder;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VectorTileEncoderUtil {

    private final Tile tile;
    private final Polygon tileBoundsPlusClipBuffer;
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
        final var envelope = new Envelope(0, config.getExtent(), 0, config.getExtent());
        envelope.expandBy(config.getClipBuffer());
        tileBoundsPlusClipBuffer = JTS.toGeometry(envelope);
    }

    public byte[] encode() {
        for (final var element : tile.elements()) {
            final var geometry = getTileGeometry(element.geometry());
            if (geometry.isEmpty()) {
                logger.warn("Skipping {}, since no tile geometry could be created", element);
            } else {
                final var tags = VectorTileElementUtils.getFinalTags(element);
                encoder.addFeature(element.layer(), tags, geometry.get(), element.id());
            }
        }

        return encoder.encode();
    }

    private Optional<Geometry> getTileGeometry(@Nullable Geometry geometry) {
        return Optional.ofNullable(geometry)
                .flatMap(validateGeometry("Source"))
                .map(this::convertToTileCoordinates)
                .flatMap(validateGeometry("Tile"))
                .map(this::simplifyIfNeeded)
                .flatMap(validateGeometry("Simplified"))
                .map(this::smoothingIfNeeded)
                .flatMap(validateGeometry("Smoothed"))
                .map(this::clip)
                .flatMap(validateGeometry("Clipped"))
                .map(this::toIntegerGrid)
                .flatMap(validateGeometry("Integer"));
    }

    private Function<Geometry, Optional<Geometry>> validateGeometry(@NotNull final String context) {
        return geometry -> {
            if (geometry.isValid()) {
                return Optional.of(geometry);
            }
            try {
                logger.info("{} geometry is not valid, trying to fix now", context);
                final var validGeometry = JtsUtil.tryToGetValidGeometry(geometry);
                if (validGeometry.isEmpty()) {
                    logger.warn("{} geometry is empty.", context);
                    return Optional.empty();
                }
                return Optional.of(geometry);
            } catch (UnfixableGeometryException e) {
                logger.warn("{} geometry is not valid and could not be fixed", context, e);
                return Optional.empty();
            }
        };
    }

    private Geometry convertToTileCoordinates(@NotNull final Geometry geometry) {
        return TileGeometryUtils.getTileGeometry(geometry, tile.boundingBox(), config);
    }

    private Geometry clip(final Geometry geometry) {
        return tileBoundsPlusClipBuffer.intersection(geometry);
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

    private Geometry toIntegerGrid(@NotNull final Geometry geometry) {
        return GeometryPrecisionReducer.reduce(geometry, new PrecisionModel(1));
    }
}
