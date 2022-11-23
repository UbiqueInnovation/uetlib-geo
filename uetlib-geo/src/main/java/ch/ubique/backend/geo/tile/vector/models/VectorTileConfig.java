package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;


public class VectorTileConfig {

    private int maxElementsPerTile = -1;
    private double simplifyTolerance = 0;
    private int clipBuffer = 0;
    private int extent = 4096;
    private final YAxisDirection yAxisDirection;

    private Set<Integer> zoomLevels;

    private VectorTileConfig(YAxisDirection yAxisDirection) {
        this.yAxisDirection = yAxisDirection;
    }

    /**
     * This sets {@linkplain VectorTileConfig#yAxisDirection} to {@link YAxisDirection#UP}, which is the
     * correct direction, according to the vector tile standard.
     * <p>
     * Always use this method, for new layers, and when you do not need backwards compatibility.
     */
    public static VectorTileConfig correct() {
        return new VectorTileConfig(YAxisDirection.DOWN);
    }

    /**
     * This sets {@linkplain VectorTileConfig#yAxisDirection} to {@link YAxisDirection#DOWN}, which is the
     * wrong direction, but has been used for multiple layers already.
     * <p>
     * Use this method only if you need backwards compatibility.
     */
    public static VectorTileConfig wrongDirection() {
        return new VectorTileConfig(YAxisDirection.UP);
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
    public int getMaxElementsPerTile(int zoomLevel) {
        final var isMaxZoomLevel = zoomLevels.stream().allMatch(level -> zoomLevel >= level);
        if (isMaxZoomLevel) {
            return -1;
        } else {
            return this.maxElementsPerTile;
        }
    }

    public boolean limitElementsPerTile(int zoomLevel) {
        return this.getMaxElementsPerTile(zoomLevel) >= 0;
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
     * (in output coordinates on of the vector tile, the output coordinates are between 0 and extent).
     * The tolerance value must be non-negative. A tolerance value of zero is effectively a no-op.
     */
    public VectorTileConfig setSimplifyTolerance(double simplifyTolerance) {
        this.simplifyTolerance = simplifyTolerance;
        return this;
    }


    public double simplifyTolerance() {
        return simplifyTolerance;
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
