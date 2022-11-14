package ch.ubique.backend.geo.tile.common.models;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @param origin Origin for all zoom levels
 * @param yAxisDirection Y-Axis direction
 * @param zoomLevels Definition of all available zoom levels
 * @param srid Spatial reference identifier
 */
public record TileLevelsDefinition(
        LV95Coordinate origin,
        YAxisDirection yAxisDirection,
        List<ZoomLevel> zoomLevels,
        int srid) {

    public TileLevelsDefinition(
            LV95Coordinate origin,
            YAxisDirection yAxisDirection,
            List<ZoomLevel> zoomLevels,
            int srid) {
        if (zoomLevels == null || zoomLevels.isEmpty()) {
            throw new IllegalArgumentException("Zoom levels are not allowed to be null or empty");
        }
        this.origin = origin;
        this.yAxisDirection = yAxisDirection;
        this.zoomLevels =
                zoomLevels.stream()
                        .distinct()
                        .sorted(Comparator.comparingInt(ZoomLevel::level))
                        .toList();
        this.srid = srid;
    }

    public ZoomLevel minZoomLevel() {
        return zoomLevels.get(0);
    }

    public ZoomLevel maxZoomLevel() {
        return zoomLevels.get(zoomLevels.size() - 1);
    }

    public int minLevel() {
        return minZoomLevel().level();
    }

    public int maxLevel() {
        return maxZoomLevel().level();
    }

    public Optional<ZoomLevel> nextSmaller(ZoomLevel level) {
        final var index = zoomLevels.indexOf(level) - 1;
        if (index < 0) {
            return Optional.empty();
        }

        return Optional.of(zoomLevels.get(index));
    }

    public Optional<ZoomLevel> nextLarger(ZoomLevel level) {
        final var index = zoomLevels.indexOf(level) + 1;
        if (index >= zoomLevels.size()) {
            return Optional.empty();
        }

        return Optional.of(zoomLevels.get(index));
    }

    /** Return given zoom level object, returns Optional.empty() if level does not exist */
    public ZoomLevel getZoomLevel(int level) {
        return zoomLevels.stream()
                .filter(zoomLevel -> zoomLevel.level() == level)
                .findAny()
                .orElse(null);
    }

    public boolean isMin(ZoomLevel zoomLevel) {
        return zoomLevel.equals(minZoomLevel());
    }

    public boolean isMax(ZoomLevel zoomLevel) {
        return zoomLevel.equals(maxZoomLevel());
    }

    public TileLevelsDefinition onlyWithLevels(Integer... levels) {
        return onlyWithLevels(List.of(levels));
    }

    public TileLevelsDefinition withMaxLevel(int maxLevel) {
        return new TileLevelsDefinition(
                origin,
                yAxisDirection,
                zoomLevels.stream().filter(z -> z.level() <= maxLevel).toList(),
                srid);
    }

    public TileLevelsDefinition withMinLevel(int minLevel) {
        return new TileLevelsDefinition(
                origin,
                yAxisDirection,
                zoomLevels.stream().filter(z -> z.level() >= minLevel).toList(),
                srid);
    }

    public TileLevelsDefinition withMinMaxLevel(int minLevel, int maxLevel) {
        return new TileLevelsDefinition(
                origin,
                yAxisDirection,
                zoomLevels.stream()
                        .filter(z -> z.level() >= minLevel && z.level() <= maxLevel)
                        .toList(),
                srid);
    }

    public TileLevelsDefinition onlyWithLevels(Collection<Integer> levels) {
        return new TileLevelsDefinition(
                origin,
                yAxisDirection,
                zoomLevels.stream().filter(z -> levels.contains(z.level())).toList(),
                srid);
    }

    public TileLevelsDefinition addLevel(ZoomLevel zoomLevel) {
        final var combinedLevels = new ArrayList<>(zoomLevels);
        combinedLevels.add(zoomLevel);
        return new TileLevelsDefinition(origin, yAxisDirection, combinedLevels, srid);
    }

    public long numberOfTiles() {
        return zoomLevels.stream().mapToLong(ZoomLevel::numberOfTiles).sum();
    }

    /**
     * Returns a collection of all tile infos of the next larger level (larger level = smaller area
     * covered) that overlap with the provided TileInfo
     */
    public Set<TileInfo> childrenTileInfos(TileInfo current) {
        final var currentLevel = current.zoomLevel();
        return overlappingTileInfos(current, nextLarger(currentLevel).orElse(null));
    }

    /**
     * Returns a collection of all tile infos of the next smaller level (smaller level = larger area
     * covered) that overlap with the provided TileInfo
     */
    public Set<TileInfo> parentTileInfos(TileInfo current) {
        final var currentLevel = current.zoomLevel();
        return overlappingTileInfos(current, nextSmaller(currentLevel).orElse(null));
    }

    /**
     * Returns a collection of all tile infos at level otherLevel that overlap with the given
     * TileInfo
     */
    private Set<TileInfo> overlappingTileInfos(TileInfo current, ZoomLevel otherLevel) {
        final var currentLevel = current.zoomLevel();
        if (otherLevel == null) {
            return Set.of();
        }

        final var minX = (int) (current.x() * currentLevel.tileWidth() / otherLevel.tileWidth());
        final var minY = (int) (current.y() * currentLevel.tileWidth() / otherLevel.tileWidth());

        final var maxX =
                Math.min(
                        otherLevel.tilesX(),
                        (int)
                                Math.ceil(
                                        (current.x() + 1)
                                                * currentLevel.tileWidth()
                                                / otherLevel.tileWidth()));
        final var maxY =
                Math.min(
                        otherLevel.tilesY(),
                        (int)
                                Math.ceil(
                                        (current.y() + 1)
                                                * currentLevel.tileWidth()
                                                / otherLevel.tileWidth()));

        return IntStream.range(minX, maxX)
                .boxed()
                .flatMap(
                        x ->
                                IntStream.range(minY, maxY)
                                        .mapToObj(y -> new TileInfo(x, y, otherLevel)))
                .collect(Collectors.toUnmodifiableSet());
    }
}
