package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WebMercatorTileDefinitions;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class VectorTileConfig {


    private Integer maxElementsPerTile;
    private Double simplifyTolerance;
    private Double smoothing;
    private int clipBuffer = 0;
    private int extent = 4096;
    private TileOutputFormat outputFormat = TileOutputFormat.PBF;
    private final YAxisDirection yAxisDirection;

    private Set<Integer> zoomLevels;

    private final TileLevelsDefinition tileLevelsDefinition;

    private VectorTileConfig(
            YAxisDirection yAxisDirection, final TileLevelsDefinition tileLevelsDefinition) {
        this.yAxisDirection = yAxisDirection;
        this.tileLevelsDefinition = tileLevelsDefinition;
    }

    public static VectorTileConfig lv95() {
        return new VectorTileConfig(YAxisDirection.DOWN, SwissConstants.swissTileDefinition());
    }

    public static VectorTileConfig webMercator() {
        return new VectorTileConfig(
                YAxisDirection.DOWN, WebMercatorTileDefinitions.tileDefinitions());
    }

    /**
     * This sets {@linkplain VectorTileConfig#yAxisDirection} to {@link YAxisDirection#DOWN}, which
     * is the wrong direction, but has been used for multiple layers already.
     *
     * <p>Use this method only if you need backwards compatibility.
     *
     * @deprecated Will be removed with 2.0.0
     */
    @Deprecated(forRemoval = true, since = "1.2.0")
    public static VectorTileConfig lv95WrongDirection() {
        return new VectorTileConfig(YAxisDirection.UP, SwissConstants.swissTileDefinition());
    }

    public VectorTileConfig setMaxElementsPerTile(int maxElementsPerTile) {
        if (maxElementsPerTile <= 0) {
            throw new IllegalArgumentException("The limit needs to be positive.");
        }
        this.maxElementsPerTile = maxElementsPerTile;
        return this;
    }

    public VectorTileConfig setClipBuffer(int clipBuffer) {
        this.clipBuffer = clipBuffer;
        return this;
    }

    public VectorTileConfig setExtent(int extent) {
        this.extent = extent;
        return this;
    }

    public VectorTileConfig setZoomLevels(Collection<Integer> zoomLevels) {
        this.zoomLevels = new HashSet<>(zoomLevels);
        return this;
    }

    public VectorTileConfig setZoomLevels(Integer... zoomLevels) {
        return setZoomLevels(Arrays.asList(zoomLevels));
    }

    public int getExtent() {
        return extent;
    }

    public int getClipBuffer() {
        return clipBuffer;
    }

    public Optional<Double> getSimplifyTolerance() {
        return Optional.ofNullable(simplifyTolerance);
    }

    /**
     * All vertices in the simplified geometry will be within this distance of the original geometry
     * (in output coordinates on of the vector tile, the output coordinates are between 0 and
     * extent). The tolerance value must be positive.
     */
    public VectorTileConfig setSimplifyTolerance(double simplifyTolerance) {
        if (simplifyTolerance <= 0) {
            throw new IllegalArgumentException("The simplify tolerance needs to be positive.");
        }
        this.simplifyTolerance = simplifyTolerance;
        return this;
    }

    public Optional<Double> getSmoothing() {
        return Optional.ofNullable(smoothing);
    }

    /**
     * @param alpha curved-ness parameter (0 is linear, 1 is round, >1 is increasingly curved)
     */
    public VectorTileConfig setSmoothing(final double alpha) {
        if (alpha <= 0) {
            throw new IllegalArgumentException("The smoothing value alpha needs to be positive.");
        }
        this.smoothing = alpha;
        return this;
    }

    public YAxisDirection getYAxisDirection() {
        return yAxisDirection;
    }

    public TileLevelsDefinition getTileLevelsDefinition() {
        return tileLevelsDefinition;
    }

    public Set<Integer> getZoomLevels() {
        return zoomLevels;
    }

    /**
     * Returns the maximum allowed elements per tile. This depends on the zoomlevel, meaning that on
     * the maximum level we never limit the allowed number of elements, so we never miss any
     * elements.
     */
    public Optional<Integer> getLimit(int zoomLevel) {
        final var isMaxZoomLevel = zoomLevels.stream().allMatch(level -> zoomLevel >= level);
        if (isMaxZoomLevel) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(this.maxElementsPerTile);
        }
    }

    public TileOutputFormat getOutputFormat() {
        return outputFormat;
    }

    public VectorTileConfig setOutputFormat(final TileOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("maxElementsPerTile", maxElementsPerTile)
                .append("simplifyTolerance", simplifyTolerance)
                .append("clipBuffer", clipBuffer)
                .append("extent", extent)
                .append("yAxisDirection", yAxisDirection)
                .append("zoomLevels", zoomLevels)
                .toString();
    }
}
