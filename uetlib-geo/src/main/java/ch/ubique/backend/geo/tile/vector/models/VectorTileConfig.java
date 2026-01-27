package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;
import ch.ubique.backend.geo.tile.common.constants.Wgs84TileDefinitions;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileGenerator;

import tools.jackson.databind.ObjectMapper;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.slf4j.event.Level;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.ubique.backend.core.utils.NumberUtils.requirePositive;
import static java.util.Objects.requireNonNull;

public record VectorTileConfig(
        @NotNull YAxisDirection yAxisDirection,
        @NotNull TileLevelsDefinition tileLevelsDefinition,
        @Nullable Integer maxElementsPerTile,
        @PositiveOrZero double simplifyTolerance,
        @PositiveOrZero double smoothing,
        int clipBuffer,
        boolean validateElements,
        boolean validateGeometryAtEveryStep,
        boolean checkCoordinateSystemBounds,
        @NotNull Level emptyGeometryLogLevel,
        @Positive int extent,
        @NotNull TileOutputFormat outputFormat,
        @Nullable Collection<ReferencedEnvelope> outputEnvelopes,
        @NotNull Set<Integer> zoomLevels,
        @NotNull ObjectMapper objectMapper,
        @Positive int vectorTileGeneratorParallelism) {

    public static VectorTileConfigBuilder lv95() {
        return new VectorTileConfigBuilder()
                .tileLevelsDefinition(SwissConstants.swissTileDefinition());
    }

    public static VectorTileConfigBuilder webMercator() {
        return new VectorTileConfigBuilder()
                .tileLevelsDefinition(WebMercatorTileDefinitions.tileDefinitions());
    }

    public static VectorTileConfigBuilder wgs84() {
        return new VectorTileConfigBuilder()
                .tileLevelsDefinition(Wgs84TileDefinitions.tileDefinitions());
    }

    /**
     * This sets {@linkplain VectorTileConfig#yAxisDirection} to {@link YAxisDirection#UP}, which is
     * the wrong direction, but has been used for multiple layers already.
     *
     * <p>Use this method only if you need backwards compatibility.
     *
     * @deprecated Will be removed with 3.0.0
     */
    @Deprecated(forRemoval = true, since = "1.2.0")
    public static VectorTileConfigBuilder lv95WrongDirection() {
        return new VectorTileConfigBuilder(YAxisDirection.UP)
                .tileLevelsDefinition(SwissConstants.swissTileDefinition());
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
        private YAxisDirection yAxisDirection = YAxisDirection.DOWN;
        private TileLevelsDefinition tileLevelsDefinition;
        private Integer maxElementsPerTile;
        private double simplifyTolerance;
        private double smoothing;
        private int clipBuffer;
        private boolean validateElements = true;
        private boolean validateGeometryAtEveryStep = true;
        private boolean checkCoordinateSystemBounds = true;
        private int extent = 4096;
        private TileOutputFormat outputFormat = TileOutputFormat.PBF;
        private Collection<ReferencedEnvelope> outputEnvelopes;
        private Set<Integer> zoomLevels;
        private ObjectMapper objectMapper;
        private Level emptyGeometryLogLevel = Level.WARN;
        private int vectorTileGeneratorParallelism = 64;

        private VectorTileConfigBuilder() {}

        private VectorTileConfigBuilder(@NotNull final YAxisDirection yAxisDirection) {
            requireNonNull(yAxisDirection, "yAxisDirection must be non-null");
            this.yAxisDirection = yAxisDirection;
        }

        public VectorTileConfigBuilder tileLevelsDefinition(
                @NotNull final TileLevelsDefinition tileLevelsDefinition) {
            requireNonNull(tileLevelsDefinition, "tileLevelsDefinition must be non-null");
            this.tileLevelsDefinition = tileLevelsDefinition;
            return this;
        }

        public VectorTileConfigBuilder maxElementsPerTile(final Integer maxElementsPerTile) {
            requireNonNull(maxElementsPerTile, "maxElementsPerTile must be non-null");
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
            if (smoothing < 0) {
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

        public VectorTileConfigBuilder validateGeometryAtEveryStep(
                final boolean validateGeometryAtEveryStep) {
            this.validateGeometryAtEveryStep = validateGeometryAtEveryStep;
            return this;
        }

        public VectorTileConfigBuilder checkCoordinateSystemBounds(
                final boolean checkCoordinateSystemBounds) {
            this.checkCoordinateSystemBounds = checkCoordinateSystemBounds;
            return this;
        }

        public VectorTileConfigBuilder extent(@Positive final int extent) {
            //noinspection ConstantValue
            if (extent < 1) {
                throw new IllegalArgumentException("extent must be positive");
            }
            this.extent = extent;
            return this;
        }

        public VectorTileConfigBuilder outputFormat(@NotNull final TileOutputFormat outputFormat) {
            requireNonNull(outputFormat, "outputFormat must be non-null");
            this.outputFormat = outputFormat;
            return this;
        }

        public VectorTileConfigBuilder outputEnvelope(final ReferencedEnvelope outputEnvelope) {
            requireNonNull(outputEnvelope, "outputEnvelope must be non-null");
            this.outputEnvelopes = List.of(outputEnvelope);
            return this;
        }

        public VectorTileConfigBuilder outputEnvelopes(
                final Collection<ReferencedEnvelope> outputEnvelopes) {
            this.outputEnvelopes = outputEnvelopes;
            return this;
        }

        public VectorTileConfigBuilder zoomLevels(final Set<Integer> zoomLevels) {
            this.zoomLevels = zoomLevels;
            return this;
        }

        public VectorTileConfigBuilder zoomLevels(final IntStream zoomLevels) {
            this.zoomLevels = zoomLevels.boxed().collect(Collectors.toUnmodifiableSet());
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
            requireNonNull(objectMapper, "objectMapper must be non-null");
            this.objectMapper = objectMapper;
            return this;
        }

        public VectorTileConfigBuilder emptyGeometryLogLevel(
                @NotNull final Level emptyGeometryLogLevel) {
            this.emptyGeometryLogLevel =
                    requireNonNull(
                            emptyGeometryLogLevel, "emptyGeometryLogLevel must not be null");
            return this;
        }

        /**
         * @param vectorTileGeneratorParallelism defaults to 64, decrease if you encounter memory
         *     issues possibly related to the {@link VectorTileGenerator} using too many threads.
         */
        public VectorTileConfigBuilder vectorTileGeneratorParallelism(
                @Positive final int vectorTileGeneratorParallelism) {
            //noinspection ConstantValue
            if (vectorTileGeneratorParallelism < 1) {
                throw new IllegalArgumentException(
                        "vectorTileGeneratorParallelism must be positive");
            }
            this.vectorTileGeneratorParallelism = vectorTileGeneratorParallelism;
            return this;
        }

        private void validate() {
            requireNonNull(yAxisDirection, "yAxisDirection must not be null");
            requirePositive(extent, "extent must be positive");
            requireNonNull(outputFormat, "outputFormat must be positive");
            requireNonNull(emptyGeometryLogLevel, "emptyGeometryLogLevel must not be null");
            requirePositive(vectorTileGeneratorParallelism, "vectorTileGeneratorParallelism must be positive");
        }

        public VectorTileConfig build() {
            validate();
            return new VectorTileConfig(
                    yAxisDirection,
                    tileLevelsDefinition,
                    maxElementsPerTile,
                    simplifyTolerance,
                    smoothing,
                    clipBuffer,
                    validateElements,
                    validateGeometryAtEveryStep,
                    checkCoordinateSystemBounds,
                    emptyGeometryLogLevel,
                    extent,
                    outputFormat,
                    outputEnvelopes,
                    zoomLevels,
                    objectMapper,
                    vectorTileGeneratorParallelism);
        }
    }
}
