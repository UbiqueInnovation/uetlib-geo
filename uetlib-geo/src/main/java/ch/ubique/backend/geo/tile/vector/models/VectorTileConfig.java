package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.constants.SwissConstants;
import ch.ubique.backend.geo.tile.common.constants.WorldConstants;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class VectorTileConfig {

    private int maxElementsPerTile = -1;
    private double simplifyTolerance = 0;
    private int clipBuffer = 0;
    private int extent = 4096;
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

    public static VectorTileConfig wgs84() {
        return new VectorTileConfig(YAxisDirection.DOWN, WorldConstants.tileDefinitions());
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
        this.maxElementsPerTile = maxElementsPerTile;
        return this;
    }

    /**
     * Returns the maximum allowed elements per tile. This depends on the zoomlevel, meaning that on
     * the maximum level we never limit the allowed number of elements, so we never miss any
     * elements.
     */
    public Optional<Integer> getLimit(int zoomLevel) {
        final var isMaxZoomLevel = zoomLevels.stream().allMatch(level -> zoomLevel >= level);
        if (isMaxZoomLevel || maxElementsPerTile < 0) {
            return Optional.empty();
        } else {
            return Optional.of(this.maxElementsPerTile);
        }
    }

    public boolean simplify() {
        return simplifyTolerance > 0;
    }

    public int getClipBuffer() {
        return clipBuffer;
    }

    public VectorTileConfig setClipBuffer(int clipBuffer) {
        this.clipBuffer = clipBuffer;
        return this;
    }

    public int getExtent() {
        return extent;
    }

    public VectorTileConfig setExtent(int extent) {
        this.extent = extent;
        return this;
    }

    public Set<Integer> getZoomLevels() {
        return zoomLevels;
    }

    public VectorTileConfig setZoomLevels(Collection<Integer> zoomLevels) {
        this.zoomLevels = new HashSet<>(zoomLevels);
        return this;
    }

    public VectorTileConfig setZoomLevels(Integer... zoomLevels) {
        return setZoomLevels(Arrays.asList(zoomLevels));
    }

    public YAxisDirection getYAxisDirection() {
        return yAxisDirection;
    }

    /**
     * All vertices in the simplified geometry will be within this distance of the original geometry
     * (in output coordinates on of the vector tile, the output coordinates are between 0 and
     * extent). The tolerance value must be non-negative. A tolerance value of zero is effectively a
     * no-op.
     */
    public VectorTileConfig setSimplifyTolerance(double simplifyTolerance) {
        this.simplifyTolerance = simplifyTolerance;
        return this;
    }

    public double simplifyTolerance() {
        return simplifyTolerance;
    }

    public TileLevelsDefinition getTileLevelsDefinition() {
        return tileLevelsDefinition;
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
