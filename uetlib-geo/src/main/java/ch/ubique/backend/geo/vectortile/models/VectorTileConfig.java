package ch.ubique.backend.geo.vectortile.models;

import java.util.Arrays;
import java.util.List;

public class VectorTileConfig<T extends TileElement> {

	private String key;
	private String path;
	private int maxElementsPerTile;
	private boolean simplify;
	private List<Integer> zoomLevels;
	private VectorTileFilter<T> filter;

	// computed value
	private int maxZoomLevel;

	public VectorTileConfig() {
		this.maxElementsPerTile = -1;
	}

	public VectorTileConfig<T> setMaxElementsPerTile(int maxElementsPerTile) {
		this.maxElementsPerTile = maxElementsPerTile;
		return this;
	}

	/**
	 * Returns the maximum allowed elements per tile. This depends on the zoomlevel,
	 * meaning that on the maximum level we never limit the allowed number of elements
	 * so we never miss any elements.
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public int getMaxElementsPerTile(int zoomLevel) {
		if (zoomLevel == this.maxZoomLevel) {
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
		this.maxZoomLevel = this.zoomLevels.stream().mapToInt(v -> v).max().getAsInt();
		return this;
	}

	public VectorTileConfig<T> setZoomLevels(Integer... zoomLevels) {
		this.zoomLevels = Arrays.asList(zoomLevels);
		this.maxZoomLevel = this.zoomLevels.stream().mapToInt(v -> v).max().getAsInt();
		return this;
	}

	public VectorTileConfig<T> setFilter(
      VectorTileFilter<T> filter) {
		this.filter = filter;
		return this;
	}

	public String getPath() {
		return path;
	}

	public VectorTileConfig<T> setPath(String path) {
		this.path = path;
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

	public boolean filter(T t) {
		if (filter != null) {
			return filter.accept(t);
		} else {
			return true;
		}
	}
}
