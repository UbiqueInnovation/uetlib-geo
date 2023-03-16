package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.coordinate.util.JtsUtil;
import ch.ubique.backend.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.backend.geo.tile.common.encoder.TileEncoder;
import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.utils.SimplifyGeometryUtils;
import ch.ubique.backend.geo.tile.vector.utils.TileGeometryUtils;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

abstract class AbstractVectorTileEncoder implements TileEncoder {

    private final Polygon tileBoundsPlusClipBuffer;
    private final VectorTileConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractVectorTileEncoder(final VectorTileConfig config) {
        this.config = config;
        final var envelope = new Envelope(0, config.getExtent(), 0, config.getExtent());
        envelope.expandBy(config.getClipBuffer());
        tileBoundsPlusClipBuffer = JTS.toGeometry(envelope);
    }

    @Override
    public final BinaryTile encode(final Tile tile) {
        if (!(tile instanceof VectorTile vectorTile)) {
            throw new UnsupportedOperationException(
                    "This encoder can only encode tiles of type " + VectorTile.class);
        }

        if (config.getOutputFormat() == TileOutputFormat.PBF
                && RoutingGraphVectorTileEncoder.isRoutingGraphTile(tile)) {
            return new RoutingGraphVectorTileEncoder(config).encode(vectorTile);
        }

        return new BinaryTile(
                tile.tileInfo(), config.getOutputFormat(), encodeVectorTile(vectorTile));
    }

    protected abstract byte[] encodeVectorTile(final VectorTile vectorTile);

    protected Optional<Geometry> getTileGeometry(
            @Nullable final VectorTileElement element, @NotNull final ReferencedEnvelope bounds) {
        return Optional.ofNullable(element.geometry())
                .flatMap(validateGeometry("Source"))
                .map(convertToTileCoordinates(bounds))
                .flatMap(validateGeometry("Tile"))
                .map(this::simplifyIfNeeded)
                .flatMap(validateGeometry("Simplified"))
                .map(this::smoothingIfNeeded)
                .flatMap(validateGeometry("Smoothed"))
                .map(this::clip)
                .flatMap(validateGeometry("Clipped"))
                .map(this::toIntegerGrid)
                .flatMap(validateGeometry("Integer"))
                .flatMap(includeForOutput(element));
    }

    private Function<Geometry, Optional<Geometry>> validateGeometry(@NotNull final String context) {
        return geometry -> {
            if (geometry.isValid()) {
                return Optional.of(geometry);
            }
            try {
                logger.debug("{} geometry is not valid, trying to fix now", context);
                final var validGeometry = JtsUtil.tryToGetValidGeometry(geometry);
                if (validGeometry.isEmpty()) {
                    logger.warn("{} geometry is empty.", context);
                    return Optional.empty();
                }
                return Optional.of(validGeometry);
            } catch (UnfixableGeometryException e) {
                logger.warn("{} geometry is not valid and could not be fixed", context, e);
                return Optional.empty();
            }
        };
    }

    private Function<Geometry, Geometry> convertToTileCoordinates(
            @NotNull final ReferencedEnvelope bounds) {
        return geometry -> TileGeometryUtils.getTileGeometry(geometry, bounds, config);
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

    private Geometry clip(final Geometry geometry) {
        return tileBoundsPlusClipBuffer.intersection(geometry);
    }

    private Geometry toIntegerGrid(@NotNull final Geometry geometry) {
        return GeometryPrecisionReducer.reduce(geometry, new PrecisionModel(1));
    }

    private static Function<Geometry, Optional<Geometry>> includeForOutput(
            @NotNull final VectorTileElement element) {
        return tileGeometry ->
                element.includeForOutput(tileGeometry)
                        ? Optional.of(tileGeometry)
                        : Optional.empty();
    }
}
