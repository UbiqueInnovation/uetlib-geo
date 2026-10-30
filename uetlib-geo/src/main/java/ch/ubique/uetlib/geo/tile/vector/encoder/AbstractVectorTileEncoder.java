package ch.ubique.uetlib.geo.tile.vector.encoder;

import ch.ubique.uetlib.geo.coordinate.util.JtsUtil;
import ch.ubique.uetlib.geo.geometry.exception.UnfixableGeometryException;
import ch.ubique.uetlib.geo.tile.common.encoder.TileEncoder;
import ch.ubique.uetlib.geo.tile.common.models.Tile;
import ch.ubique.uetlib.geo.tile.common.models.TileOutputFormat;
import ch.ubique.uetlib.geo.tile.vector.models.BinaryTile;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTile;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileElement;
import ch.ubique.uetlib.geo.tile.vector.utils.SimplifyGeometryUtils;
import ch.ubique.uetlib.geo.tile.vector.utils.TileGeometryUtils;
import java.util.Optional;
import java.util.function.Function;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractVectorTileEncoder implements TileEncoder {

    private final Polygon tileBoundsPlusClipBuffer;
    private final VectorTileConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractVectorTileEncoder(final VectorTileConfig config) {
        this.config = config;
        final var envelope = new Envelope(0, config.extent(), 0, config.extent());
        envelope.expandBy(config.clipBuffer());
        tileBoundsPlusClipBuffer = JTS.toGeometry(envelope);
    }

    private static Function<Geometry, Optional<Geometry>> includeForOutput(
            @NonNull final VectorTileElement element) {
        return tileGeometry ->
                element.includeForOutput(tileGeometry)
                        ? Optional.of(tileGeometry)
                        : Optional.empty();
    }

    @Override
    public final BinaryTile encode(final Tile tile) {
        if (!(tile instanceof final VectorTile vectorTile)) {
            throw new UnsupportedOperationException(
                    "This encoder can only encode tiles of type " + VectorTile.class);
        }

        if (config.outputFormat() == TileOutputFormat.PBF
                && RoutingGraphVectorTileEncoder.isRoutingGraphTile(tile)) {
            return new RoutingGraphVectorTileEncoder(config).encode(vectorTile);
        }

        return new BinaryTile(tile.tileInfo(), config.outputFormat(), encodeVectorTile(vectorTile));
    }

    protected abstract byte[] encodeVectorTile(final VectorTile vectorTile);

    protected Optional<Geometry> getTileGeometry(
            @Nullable final VectorTileElement element, @NonNull final ReferencedEnvelope bounds) {
        if (element == null) {
            return Optional.empty();
        }

        return getSourceGeometry(element)
                .flatMap(retryWithValidateOnException(convertToTileCoordinates(bounds)))
                .flatMap(retryWithValidateOnException(this::simplifyIfNeeded))
                .flatMap(retryWithValidateOnException(this::clip))
                .flatMap(retryWithValidateOnException(this::smoothingIfNeeded))
                .flatMap(retryWithValidateOnException(this::toIntegerGrid))
                .flatMap(this::finalCheck)
                .flatMap(includeForOutput(element));
    }

    private Optional<Geometry> getSourceGeometry(final @NonNull VectorTileElement element) {
        return checkGeometry("Source", element.geometry(), config.validateGeometryAtEveryStep());
    }

    private Optional<Geometry> checkGeometry(
            @NonNull final String context,
            @NonNull final Geometry geometry,
            final boolean validate) {

        if (geometry.isEmpty()) {
            logger.atLevel(config.emptyGeometryLogLevel()).log("{} geometry is empty.", context);
            return Optional.empty();
        }

        if (!validate || geometry.isValid()) {
            return Optional.of(geometry);
        }

        try {
            logger.debug("{} geometry is not valid, trying to fix now", context);
            final var validGeometry = JtsUtil.tryToGetValidGeometry(geometry);
            if (validGeometry.isEmpty()) {
                logger.atLevel(config.emptyGeometryLogLevel())
                        .log("{} geometry is empty after fixing.", context);
                return Optional.empty();
            }
            return Optional.of(validGeometry);
        } catch (final UnfixableGeometryException e) {
            logger.warn("{} geometry is not valid and could not be fixed", context, e);
            return Optional.empty();
        }
    }

    private Function<Geometry, Optional<Geometry>> convertToTileCoordinates(
            @NonNull final ReferencedEnvelope bounds) {
        return geometry -> {
            final var tileGeometry = TileGeometryUtils.getTileGeometry(geometry, bounds, config);
            return checkGeometry("Tile", tileGeometry, config.validateGeometryAtEveryStep());
        };
    }

    private Optional<Geometry> simplifyIfNeeded(@NonNull final Geometry geometry) {
        final var tolerance = config.simplifyTolerance();
        if (tolerance > 0) {
            final var simplified = SimplifyGeometryUtils.simplify(geometry, tolerance);
            return checkGeometry("Simplified", simplified, config.validateGeometryAtEveryStep());
        }

        return Optional.of(geometry);
    }

    private Optional<Geometry> smoothingIfNeeded(@NonNull final Geometry geometry) {
        if (config.smoothing() > 0) {
            try {
                final var smooth = SimplifyGeometryUtils.smooth(geometry, config.smoothing());
                return checkGeometry("Smoothed", smooth, config.validateGeometryAtEveryStep());
            } catch (final RuntimeException e) {
                logger.warn("Smoothing for {} failed.", geometry, e);
            }
        }

        return Optional.of(geometry);
    }

    private Optional<Geometry> clip(@NonNull final Geometry geometry) {
        final var clipped = tileBoundsPlusClipBuffer.intersection(geometry);
        return checkGeometry("Clipped", clipped, config.validateGeometryAtEveryStep());
    }

    private Optional<Geometry> toIntegerGrid(@NonNull final Geometry geometry) {
        final var intGeometry = GeometryPrecisionReducer.reduce(geometry, new PrecisionModel(1));
        return checkGeometry("Integer", intGeometry, config.validateGeometryAtEveryStep());
    }

    private Optional<Geometry> finalCheck(@NonNull final Geometry geometry) {
        return checkGeometry("Final check", geometry, true);
    }

    public VectorTileConfig getConfig() {
        return config;
    }

    public Function<Geometry, Optional<Geometry>> retryWithValidateOnException(
            final Function<Geometry, Optional<Geometry>> function) {
        return geometry -> {
            try {
                return function.apply(geometry);
            } catch (final Exception e) {
                try {
                    return checkGeometry("Retry", geometry, true).flatMap(function);
                } catch (final Exception e2) {
                    logger.warn("Could not encode geometry", e2);
                    return Optional.empty();
                }
            }
        };
    }
}
