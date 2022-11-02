package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.IntStream;

public abstract class RasterTileGenerator {

    private final TileLevelsDefinition gridInfo;

    protected RasterTileGenerator(final TileLevelsDefinition gridInfo, final double sourceTileSize) {
        this.gridInfo = gridInfo.addLevel(new ZoomLevel(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, sourceTileSize));
    }

    protected abstract void onTileGenerated(RasterTile rasterTile);

    protected abstract RasterTile getSourceTile(TileInfo tileInfo);


    public void generateTiles() {

        final var maxLevel = gridInfo.minZoomLevel();

        final var topLevelTileInfos = IntStream
                .range(0, maxLevel.tilesX())
                .boxed()
                .flatMap(x -> IntStream
                        .range(0, maxLevel.tilesY())
                        .mapToObj(y -> new TileInfo(x, y, maxLevel)))
                .toList();

        final var stack = new LinkedList<>(topLevelTileInfos);
        final var visited = new HashSet<TileInfo>();
        final var ready = new HashMap<TileInfo, RasterTile>();
        final var pendingParents = new HashMap<TileInfo, Set<TileInfo>>();

        while (!stack.isEmpty()) {
            final var current = stack.getLast();

            final var children = gridInfo.childrenTileInfos(current);

            if (children.isEmpty() || ready.keySet().containsAll(children)) {
                RasterTile rasterTile;
                if (children.isEmpty()) {
                    rasterTile = getSourceTile(current);
                } else {
                    rasterTile = new RasterTile(current, null); // TODO merge children to create raster tile

                    onTileGenerated(rasterTile);

                    // Clean up ready elements that are no longer needed
                    for (final var child : children) {
                        pendingParents.get(child).remove(current);
                        if (pendingParents.get(child).isEmpty()) {
                            pendingParents.remove(child);
                            ready.remove(child);
                        }
                    }
                }
                ready.put(current, rasterTile);
                stack.removeLast();
            }
            else {
                for (final var child : children) {
                    if (!visited.contains(child)) {
                        stack.add(child);
                        visited.add(child);
                        pendingParents.put(child, new HashSet<>(gridInfo.parentTileInfos(child)));
                    }
                }
            }
        }
    }
}
