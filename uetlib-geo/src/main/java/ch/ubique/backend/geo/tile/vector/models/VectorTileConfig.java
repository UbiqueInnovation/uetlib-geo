package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.core.jackson.ObjectMapperUtils;
import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.geotools.geometry.jts.ReferencedEnvelope;

import java.util.*;

public record VectorTileConfig(
        @NotNull YAxisDirection yAxisDirection,
        @NotNull TileLevelsDefinition tileLevelsDefinition,
        Integer maxElementsPerTile,
        @PositiveOrZero double simplifyTolerance,
        @PositiveOrZero double smoothing,
        int clipBuffer,
        boolean validateElements,
        @Positive int extent,
        @NotNull TileOutputFormat outputFormat,
        ReferencedEnvelope outputEnvelope,
        @NotNull Set<Integer> zoomLevels,
        @NotNull ObjectMapper objectMapper) {

    public static VectorTileConfigBuilder lv95() {
        return new VectorTileConfigBuilder()
                .yAxisDirection(YAxisDirection.DOWN)
                .tileLevelsDefinition(SwissConstants.swissTileDefinition());
    }

    public static VectorTileConfigBuilder webMercator() {
        return new VectorTileConfigBuilder()
                .yAxisDirection(YAxisDirection.DOWN)
                .tileLevelsDefinition(WebMercatorTileDefinitions.tileDefinitions());
    }

    /**
     * This sets {@linkplain VectorTileConfig#yAxisDirection} to {@link YAxisDirection#UP}, which is
     * the wrong direction, but has been used for multiple layers already.
     *
     * <p>Use this method only if you need backwards compatibility.
     *
     * @deprecated Will be removed with 2.0.0
     */
    @Deprecated(forRemoval = true, since = "1.2.0")
    public static VectorTileConfigBuilder lv95WrongDirection() {
        return new VectorTileConfigBuilder()
                .yAxisDirection(YAxisDirection.UP)
                .tileLevelsDefinition(SwissConstants.swissTileDefinition());
    }

    /**
     * @deprecated since 1.5, use {@link #extent()}
     */
    @Deprecated(forRemoval = true, since = "1.5.0")
    public int getExtent() {
        return extent;
    }

    /**
     * @deprecated since 1.5, use {@link #tileLevelsDefinition()}
     */
    @Deprecated(forRemoval = true, since = "1.5.0")
    public TileLevelsDefinition getTileLevelsDefinition() {
        return tileLevelsDefinition;
    }

    /**
     * Returns the maximum allowed elements per tile. This depends on the zoom level, meaning that
     * on the maximum level we never limit the allowed number of elements, so we never miss any
     * elements.
     */
    public Optional<Integer> getLimit(final int zoomLevel) {
        final var isMaxZoomLevel = zoomLevels.stream().allMatch(level -> zoomLevel >= level);
        if (isMaxZoomLevel) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(maxElementsPerTile);
        }
    }

    public static class VectorTileConfigBuilder {
        private YAxisDirection yAxisDirection;
        private TileLevelsDefinition tileLevelsDefinition;
        private Integer maxElementsPerTile;
        private double simplifyTolerance;
        private double smoothing;
        private int clipBuffer;
        private boolean validateElements = true;
        private int extent = 4096;
        private TileOutputFormat outputFormat = TileOutputFormat.PBF;
        private ReferencedEnvelope outputEnvelope;
        private Set<Integer> zoomLevels;
        private ObjectMapper objectMapper = ObjectMapperUtils.objectMapper();

        private VectorTileConfigBuilder() {}

        public VectorTileConfigBuilder yAxisDirection(
                @NotNull final YAxisDirection yAxisDirection) {
            Objects.requireNonNull(yAxisDirection, "yAxisDirection must be non-null");
            this.yAxisDirection = yAxisDirection;
            return this;
        }

        public VectorTileConfigBuilder tileLevelsDefinition(
                @NotNull final TileLevelsDefinition tileLevelsDefinition) {
            Objects.requireNonNull(yAxisDirection, "tileLevelsDefinition must be non-null");
            this.tileLevelsDefinition = tileLevelsDefinition;
            return this;
        }

        public VectorTileConfigBuilder maxElementsPerTile(final Integer maxElementsPerTile) {
            this.maxElementsPerTile = maxElementsPerTile;
            return this;
        }

        /**
         * All vertices in the simplified geometry will be within this distance of the original
         * geometry (in output coordinates on of the vector tile, the output coordinates are between
         * 0 and extent). The tolerance value must be positive.
         */
        public VectorTileConfigBuilder simplifyTolerance(
                @PositiveOrZero final double simplifyTolerance) {
            if (simplifyTolerance < 0) {
                throw new IllegalArgumentException("simplifyTolerance must be positive or zero");
            }
            this.simplifyTolerance = simplifyTolerance;
            return this;
        }

        /**
         * @param smoothing curved-ness parameter (0 is linear, 1 is round, >1 is increasingly
         *     curved)
         */
        public VectorTileConfigBuilder smoothing(@PositiveOrZero final double smoothing) {
            if (simplifyTolerance < 0) {
                throw new IllegalArgumentException("smoothing must be positive or zero");
            }
            this.smoothing = smoothing;
            return this;
        }

        public VectorTileConfigBuilder clipBuffer(final int clipBuffer) {
            this.clipBuffer = clipBuffer;
            return this;
        }

        public VectorTileConfigBuilder validateElements(final boolean validateElements) {
            this.validateElements = validateElements;
            return this;
        }

        public VectorTileConfigBuilder extent(@Positive final int extent) {
            if (simplifyTolerance < 1) {
                throw new IllegalArgumentException("extent must be positive");
            }
            this.extent = extent;
            return this;
        }

        public VectorTileConfigBuilder outputFormat(@NotNull final TileOutputFormat outputFormat) {
            Objects.requireNonNull(yAxisDirection, "outputFormat must be non-null");
            this.outputFormat = outputFormat;
            return this;
        }

        public VectorTileConfigBuilder outputEnvelope(final ReferencedEnvelope outputEnvelope) {
            this.outputEnvelope = outputEnvelope;
            return this;
        }

        public VectorTileConfigBuilder zoomLevels(final Set<Integer> zoomLevels) {
            this.zoomLevels = zoomLevels;
            return this;
        }

        public VectorTileConfigBuilder zoomLevels(final Collection<Integer> zoomLevels) {
            this.zoomLevels = Set.copyOf(zoomLevels);
            return this;
        }

        public VectorTileConfigBuilder zoomLevels(final Integer... zoomLevels) {
            this.zoomLevels = Set.of(zoomLevels);
            return this;
        }

        public VectorTileConfigBuilder objectMapper(@NotNull final ObjectMapper objectMapper) {
            Objects.requireNonNull(yAxisDirection, "objectMapper must be non-null");
            this.objectMapper = objectMapper;
            return this;
        }

        public VectorTileConfig build() {
            return new VectorTileConfig(
                    yAxisDirection,
                    tileLevelsDefinition,
                    maxElementsPerTile,
                    simplifyTolerance,
                    smoothing,
                    clipBuffer,
                    validateElements,
                    extent,
                    outputFormat,
                    outputEnvelope,
                    zoomLevels,
                    objectMapper);
        }
    }
}
