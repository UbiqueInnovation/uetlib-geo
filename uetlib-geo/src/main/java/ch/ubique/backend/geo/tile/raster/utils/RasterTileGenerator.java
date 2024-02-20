package ch.ubique.backend.geo.tile.raster.utils;

import ch.ubique.backend.geo.tile.common.models.TileInfo;
import ch.ubique.backend.geo.tile.common.models.TileLevelsDefinition;
import ch.ubique.backend.geo.tile.common.models.ZoomLevel;
import ch.ubique.backend.geo.tile.raster.models.RasterTile;

import jakarta.validation.constraints.NotNull;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Generates raster tiles for all zoom levels specified in the {@link TileLevelsDefinition}. For
 * this, the highest zoom level is computed based on the source tiles. All other zoom levels are
 * generated based on the next higher zoom level.
 */
public abstract class RasterTileGenerator {

    private final TileLevelsDefinition levelsDefinition;
    private final int outputResolution;

    private final double sourceTileWidth;

    private final double[] customEmptyPixelValue;

    protected RasterTileGenerator(
            final TileLevelsDefinition levelsDefinition,
            final double sourceTileWidth,
            final double sourceTileHeight,
            final int outputResolution) {
        this(levelsDefinition, sourceTileWidth, sourceTileHeight, outputResolution, null);
    }

    protected RasterTileGenerator(
            final TileLevelsDefinition levelsDefinition,
            final double sourceTileWidth,
            final double sourceTileHeight,
            final int outputResolution,
            final double[] customEmptyPixelValue) {
        this.outputResolution = outputResolution;
        this.sourceTileWidth = sourceTileWidth;
        this.customEmptyPixelValue = customEmptyPixelValue;

        // Add a "fake" zoom level for the input.
        // We set the level to Integer.MAX_VALUE, to ensure the level is treated as the base case.
        final var sourceZoomLevel =
                ZoomLevel.unbounded(Integer.MAX_VALUE, sourceTileWidth, sourceTileHeight);
        this.levelsDefinition = levelsDefinition.addLevel(sourceZoomLevel);
    }

    private static void cleanFinishedTiles(
            final Map<TileInfo, RasterTile> ready,
            final Map<TileInfo, Set<TileInfo>> pendingParents,
            final TileInfo current,
            final Set<TileInfo> children) {
        for (final var child : children) {
            pendingParents.get(child).remove(current);
            if (pendingParents.get(child).isEmpty()) {
                pendingParents.remove(child);
                ready.remove(child);
            }
        }
    }

    /**
     * This method generates {@link RasterTile}s for all specified zoom levels. The highest zoom
     * level is computed based on the source tiles, all other zoom levels are generated based on the
     * next higher zoom level.
     */
    public void generateTiles() {
        // Problem:
        // Since tiles of a given level are usually not aligned with the next higher level,
        // we potentially need each tile of the next higher level for multiple tiles at the given
        // level.
        // In essence, we have an implicit dependency graph. In this context, a tile corresponds to
        // a vertex in the dependency graph. If, tile A needs a tile B of the next higher level,
        // there is an edge in the dependency graph from A to B.

        // Solution:
        // We will explore the dependency graph using a DFS (depth first search).
        // This way, we always know the dependencies and only keep the tiles active that are
        // absolutely necessary.

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
                final RasterTile rasterTile;
                if (children.isEmpty()) {
                    // Base case: load the tile from the source
                    rasterTile = getSourceTile(current);
                    validateSourceTile(current, rasterTile);
                } else {
                    // Generate a raster tile based on all children of the current element
                    rasterTile =
                            createFromChildren(current, children.stream().map(ready::get).toList());
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
                    pendingParents.put(
                            child, new HashSet<>(levelsDefinition.parentTileInfos(child)));
                }
            }
        }
    }

    @NotNull
    private List<TileInfo> getTileTopLevelTileInfos() {
        final var maxLevel = levelsDefinition.minZoomLevel();
        return IntStream.range(0, maxLevel.tilesX())
                .boxed()
                .flatMap(
                        x ->
                                IntStream.range(0, maxLevel.tilesY())
                                        .mapToObj(
                                                y ->
                                                        levelsDefinition.tileInfo(
                                                                x, y, maxLevel.level())))
                .toList();
    }

    /**
     * This method provides the generator with the actual data. It must return a {@link RasterTile}
     * {@code rasterTile} such that {@code rasterTile.tileInfo().equals(tileInfo) &&
     * rasterTile.size().tileWidth == sourceTileWidth}
     *
     * @param tileInfo the requested tile
     * @return a raster tile for the given {@link TileInfo}
     */
    protected abstract RasterTile getSourceTile(TileInfo tileInfo);

    private void validateSourceTile(final TileInfo tileInfo, final RasterTile sourceTile) {
        if (!tileInfo.equals(sourceTile.tileInfo())) {
            throw new AssertionError(
                    "Expected getSourceTile(tileInfo).tileInfo() == "
                            + tileInfo
                            + ". "
                            + "But got "
                            + sourceTile.tileInfo());
        }
        if (sourceTile.size().tileWidth() != sourceTileWidth) {
            throw new AssertionError(
                    "Expected getSourceTile(tileInfo).size().tileWidth() == "
                            + sourceTileWidth
                            + ". "
                            + "But got "
                            + sourceTile.size().tileWidth());
        }
    }

    private RasterTile createFromChildren(
            final TileInfo currentInfo, final Collection<RasterTile> children) {

        final var nonEmptyChildren = children.stream().filter(tile -> !tile.isEmpty()).toList();
        if (nonEmptyChildren.isEmpty()) {
            // All children are empty -> tile will be empty
            return RasterTile.empty(currentInfo);
        }

        final var anyChild = nonEmptyChildren.getFirst();
        final var colorModel = anyChild.data().getColorModel();
        final var raster =
                colorModel.createCompatibleWritableRaster(outputResolution, outputResolution);
        final var data =
                new BufferedImage(colorModel, raster, anyChild.data().isAlphaPremultiplied(), null);

        if (customEmptyPixelValue != null) {
            for (var x = 0; x < data.getWidth(); ++x) {
                for (var y = 0; y < data.getHeight(); ++y) {
                    raster.setPixel(x, y, customEmptyPixelValue);
                }
            }
        }

        final var currentTileWidth = currentInfo.zoomLevel().tileWidth();

        for (final var child : nonEmptyChildren) {
            final var childInfo = child.tileInfo();
            final var childTileWidth = childInfo.zoomLevel().tileWidth();

            // Calculate scaling and where tile needs to go
            final var xOffset =
                    (int)
                            Math.round(
                                    (childInfo.x() * childTileWidth
                                                    - currentInfo.x() * currentTileWidth)
                                            / currentTileWidth
                                            * outputResolution);
            final var xNextOffset =
                    (int)
                            Math.round(
                                    ((childInfo.x() + 1) * childTileWidth
                                                    - currentInfo.x() * currentTileWidth)
                                            / currentTileWidth
                                            * outputResolution);
            final var yOffset =
                    (int)
                            Math.round(
                                    (childInfo.y() * childTileWidth
                                                    - currentInfo.y() * currentTileWidth)
                                            / currentTileWidth
                                            * outputResolution);
            final var yNextOffset =
                    (int)
                            Math.round(
                                    ((childInfo.y() + 1) * childTileWidth
                                                    - currentInfo.y() * currentTileWidth)
                                            / currentTileWidth
                                            * outputResolution);

            final var scaledWidth = xNextOffset - xOffset;
            final var scaledHeight = yNextOffset - yOffset;

            final var leftCrop = Math.max(0, -xOffset);
            final var topCrop = Math.max(0, -yOffset);
            final var rightCrop = Math.max(0, scaledWidth - (outputResolution - xOffset));
            final var bottomCrop = Math.max(0, scaledHeight - (outputResolution - yOffset));

            final var croppedWidth = scaledWidth - leftCrop - rightCrop;
            final var croppedHeight = scaledHeight - topCrop - bottomCrop;

            if (croppedWidth == 0 || croppedHeight == 0) {
                continue;
            }

            // Actually scale the child data
            final var target =
                    data.getSubimage(
                            Math.max(0, xOffset),
                            Math.max(0, yOffset),
                            croppedWidth,
                            croppedHeight);
            final var affineTransform = new AffineTransform();
            affineTransform.translate(-leftCrop, -topCrop);
            affineTransform.scale(
                    1.0 * scaledWidth / child.data().getWidth(),
                    1.0 * scaledHeight / child.data().getHeight());
            RasterTransform.transform(child.data().getData(), target.getRaster(), affineTransform);
        }

        return new RasterTile(currentInfo, data);
    }

    /**
     * This method is called once a raster tile has been generated. You can use it to consume
     * generated tiles, e.g. for saving the tiles to some storage.
     *
     * @param rasterTile the generated raster tile
     */
    protected abstract void onTileGenerated(RasterTile rasterTile);
}
