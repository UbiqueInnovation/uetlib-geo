package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.List;


public class VectorTileConfig {

    private String key;
    private int maxElementsPerTile = -1;
    private double simplifyTolerance;
    private int clipBuffer = 8;
    private int extent = 4096;

    /**
     * technically DOWN is the correct default, as this corresponds with the vector tile specs.
     * however, the first implementation used UP as default, so we are now stuck with the incorrect
     * default
     */
    private YAxisDirection yAxisDirection = YAxisDirection.UP;

    private List<Integer> zoomLevels;

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

    public List<Integer> getZoomLevels() {
        return zoomLevels;
    }

    public VectorTileConfig setZoomLevels(List<Integer> zoomLevels) {
        this.zoomLevels = zoomLevels;
        return this;
    }

    public VectorTileConfig setZoomLevels(Integer... zoomLevels) {
        return setZoomLevels(Arrays.asList(zoomLevels));
    }

    public String getKey() {
        return key;
    }

    public VectorTileConfig setKey(String key) {
        this.key = key;
        return this;
    }

    public YAxisDirection getYAxisDirection() {
        return yAxisDirection;
    }

    public VectorTileConfig setYAxisDirection(YAxisDirection yAxisDirection) {
        this.yAxisDirection = yAxisDirection;
        return this;
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
                .append("key", key)
                .append("maxElementsPerTile", maxElementsPerTile)
                .append("simplifyTolerance", simplifyTolerance)
                .append("clipBuffer", clipBuffer)
                .append("extent", extent)
                .append("yAxisDirection", yAxisDirection)
                .append("zoomLevels", zoomLevels)
                .toString();
    }
}
