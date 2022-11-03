package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

/**
 * Generates raster tiles for all zoom levels specified in the {@link TileLevelsDefinition}.
 * For this, the highest zoom level is computed based on the source tiles.
 * All other zoom levels are generated based on the next higher zoom level.
 */
public abstract class RasterTileGenerator {

    private final TileLevelsDefinition levelsDefinition;
    private final int outputResolution;

    private final double sourceTileWidth;

    protected RasterTileGenerator(final TileLevelsDefinition levelsDefinition, final double sourceTileWidth, int outputResolution) {
        this.outputResolution = outputResolution;
        this.sourceTileWidth = sourceTileWidth;

        // Add a "fake" zoom level for the input.
        // We set the level to Integer.MAX_VALUE, to ensure the level is treated as the base case.
        final var sourceZoomLevel = ZoomLevel.unbounded(Integer.MAX_VALUE, sourceTileWidth);
        this.levelsDefinition = levelsDefinition.addLevel(sourceZoomLevel);
    }

    /**
     * This method provides the generator with the actual data.
     * It must return a {@link RasterTile} {@code rasterTile}
     * such that {@code  rasterTile.tileInfo().equals(tileInfo) && rasterTile.size().tileWidth == sourceTileWidth}
     *
     * @param tileInfo  the requested tile
     * @return          a raster tile for the given {@link TileInfo}
     */
    protected abstract RasterTile getSourceTile(TileInfo tileInfo);

    /**
     * This method is called once a raster tile has been generated.
     * You can use it to consume generated tiles, e.g. for saving the tiles to some storage.
     *
     * @param rasterTile the generated raster tile
     */
    protected abstract void onTileGenerated(RasterTile rasterTile);

    /**
     * This method generates {@link RasterTile}s for all specified zoom levels.
     * The highest zoom level is computed based on the source tiles,
     * all other zoom levels are generated based on the next higher zoom level.
     */
    public void generateTiles() {
        // Problem:
        // Since tiles of a given level are usually not aligned with the next higher level,
        // we potentially need each tile of the next higher level for multiple tiles at the given level.
        // In essence, we have an implicit dependency graph. In this context, a tile corresponds to
        // a vertex in the dependency graph. If, tile A needs a tile B of the next higher level,
        // there is an edge in the dependency graph from A to B.

        // Solution:
        // We will explore the dependency graph using a DFS (depth first search).
        // This way, we always know the dependencies and only keep the tiles active that are absolutely necessary.

        // Stack and visited needed for DFS
        // Initially we start with all top-level tile-infos in the stack.
        final var stack = new LinkedList<>(getTileTopLevelTileInfos());
        final var visited = new HashSet<TileInfo>();

        // Memoization: Save elements that have already been computed
        // This is needed, since a tile is potentially needed by multiple others,
        // and we do not want to recompute each time
        final var ready = new HashMap<TileInfo, RasterTile>();

        // Keep track of elements that whose parents are not yet computed
        // Essentially, these elements need to be kept in memory until all parents are ready
        final var pendingParents = new HashMap<TileInfo, Set<TileInfo>>();

        // Start of the actual DFS
        while (!stack.isEmpty()) {

            // Get current vertex and dependencies (aka. children)
            final var current = stack.getLast();
            final var children = levelsDefinition.childrenTileInfos(current);

            // Raster tile can be computed, either because
            // - we are in the base case
            // - or all dependent tiles are ready
            if (children.isEmpty() || ready.keySet().containsAll(children)) {
                RasterTile rasterTile;
                if (children.isEmpty()) {
                    // Base case: load the tile from the source
                    rasterTile = getSourceTile(current);
                    validateSourceTile(current, rasterTile);
                } else {
                    // Generate a raster tile based on all children of the current element
                    rasterTile = createFromChildren(current, children.stream().collect(toMap(c -> c, ready::get)));
                    onTileGenerated(rasterTile);

                    // Clean up ready elements that are no longer needed
                    cleanFinishedTiles(ready, pendingParents, current, children);
                }
                ready.put(current, rasterTile);
                stack.removeLast();
            }

            // Continue exploring the dependency graph
            for (final var child : children) {
                if (!visited.contains(child)) {
                    stack.add(child);
                    visited.add(child);
                    pendingParents.put(child, new HashSet<>(levelsDefinition.parentTileInfos(child)));
                }
            }

        }
    }

    @NotNull
    private List<TileInfo> getTileTopLevelTileInfos() {
        final var maxLevel = levelsDefinition.minZoomLevel();
        return IntStream
                .range(0, maxLevel.tilesX())
                .boxed()
                .flatMap(x -> IntStream
                        .range(0, maxLevel.tilesY())
                        .mapToObj(y -> new TileInfo(x, y, maxLevel)))
                .toList();
    }

    private static void cleanFinishedTiles(final Map<TileInfo, RasterTile> ready, final Map<TileInfo, Set<TileInfo>> pendingParents, final TileInfo current, Set<TileInfo> children) {
        for (final var child : children) {
            pendingParents.get(child).remove(current);
            if (pendingParents.get(child).isEmpty()) {
                pendingParents.remove(child);
                ready.remove(child);
            }
        }
    }

    private void validateSourceTile(final TileInfo tileInfo, final RasterTile sourceTile) {
        if (!tileInfo.equals(sourceTile.tileInfo())) {
            throw new AssertionError("Expected getSourceTile(tileInfo).tileInfo() == " + tileInfo + ". " +
                    "But got " + sourceTile.tileInfo());
        }
        if (sourceTile.size().tileWidth() != sourceTileWidth) {
            throw new AssertionError("Expected getSourceTile(tileInfo).size().tileWidth() == " + sourceTileWidth + ". " +
                    "But got " + sourceTile.size().tileWidth());
        }
    }

    private RasterTile createFromChildren(final TileInfo currentInfo, final Map<TileInfo, RasterTile> children) {
        final var data = new BufferedImage(outputResolution, outputResolution, BufferedImage.TYPE_4BYTE_ABGR);
        final var graphics2d = data.createGraphics();

        final var currentTileWidth = currentInfo.zoomLevel().tileWidth();

        for (final var child : children.values()) {
            final var childInfo = child.tileInfo();
            final var childTileWidth = childInfo.zoomLevel().tileWidth();

            final var xOffset = (int) Math.round((childInfo.x() * childTileWidth - currentInfo.x() * currentTileWidth) / currentTileWidth * outputResolution);
            final var xNextOffset = (int) Math.round(((childInfo.x() + 1) * childTileWidth - currentInfo.x() * currentTileWidth) / currentTileWidth * outputResolution);
            final var yOffset = (int) Math.round((childInfo.y() * childTileWidth - currentInfo.y() * currentTileWidth) / currentTileWidth * outputResolution);
            final var yNextOffset = (int) Math.round(((childInfo.y() + 1) * childTileWidth - currentInfo.y() * currentTileWidth) / currentTileWidth * outputResolution);

            final var scaledWidth = xNextOffset - xOffset;
            final var scaledHeight = yNextOffset - yOffset;

            final var finalYOffset = switch (levelsDefinition.yAxisDirection()) {
                case DOWN -> yOffset;
                case UP -> outputResolution - scaledHeight - yOffset;
            };

            final var scaledImage = child.data().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT);
            graphics2d.drawImage(scaledImage, xOffset, finalYOffset, null);
        }
        graphics2d.dispose();

        return new RasterTile(currentInfo, data);
    }
}
