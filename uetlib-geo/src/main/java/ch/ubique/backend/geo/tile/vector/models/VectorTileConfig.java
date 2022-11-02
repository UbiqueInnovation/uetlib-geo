package ch.ubique.backend.geo.tile.vector.models;

import ch.ubique.backend.geo.tile.common.models.YAxisDirection;
import com.pivovarit.function.ThrowingConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VectorTileConfig<T extends TileElement> {

    private String key;

    private ThrowingConsumer<BinaryTile, IOException> onTileGenerated;
    private int maxElementsPerTile;
    private boolean simplify;

    /**
     * technically DOWN is the correct default, as this corresponds with the vector tile specs.
     * however, the first implementation used UP as default, so we are now stuck with the incorrect
     * default
     */
    private YAxisDirection yAxisDirection = YAxisDirection.UP;

    private List<Integer> zoomLevels;
    private VectorTileFilter<T> filter;

    public VectorTileConfig() {
        this.maxElementsPerTile = -1;
    }

    public VectorTileConfig<T> setMaxElementsPerTile(int maxElementsPerTile) {
        this.maxElementsPerTile = maxElementsPerTile;
        return this;
    }

    /**
     * Returns the maximum allowed elements per tile. This depends on the zoomlevel, meaning that on
     * the maximum level we never limit the allowed number of elements so we never miss any
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
        return simplify;
    }

    public VectorTileConfig<T> setSimplify(boolean simplify) {
        this.simplify = simplify;
        return this;
    }

    public List<Integer> getZoomLevels() {
        return zoomLevels;
    }

    public VectorTileConfig<T> setZoomLevels(List<Integer> zoomLevels) {
        this.zoomLevels = zoomLevels;
        return this;
    }

    public VectorTileConfig<T> setZoomLevels(Integer... zoomLevels) {
        return setZoomLevels(Arrays.asList(zoomLevels));
    }

    public VectorTileConfig<T> setFilter(VectorTileFilter<T> filter) {
        this.filter = filter;
        return this;
    }

    public ThrowingConsumer<BinaryTile, IOException> getOnTileGenerated() {
        return onTileGenerated;
    }

    public VectorTileConfig<T> setOnTileGenerated(
            ThrowingConsumer<BinaryTile, IOException> onTileGenerated) {
        this.onTileGenerated = onTileGenerated;
        return this;
    }

    public String getKey() {
        return key;
    }

    public VectorTileConfig<T> setKey(String key) {
        this.key = key;
        return this;
    }

    public boolean isSimplify() {
        return simplify;
    }

    public YAxisDirection getYAxisDirection() {
        return yAxisDirection;
    }

    public VectorTileConfig<T> setYAxisDirection(YAxisDirection yAxisDirection) {
        this.yAxisDirection = yAxisDirection;
        return this;
    }

    public boolean filter(T t) {
        if (filter != null) {
            return filter.accept(t);
        } else {
            return true;
        }
    }
}
