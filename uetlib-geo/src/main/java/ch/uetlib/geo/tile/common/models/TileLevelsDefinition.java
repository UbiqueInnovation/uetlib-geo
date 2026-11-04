package ch.uetlib.geo.tile.common.models;

import ch.uetlib.core.utils.IntervalUtils;
import ch.uetlib.geo.coordinate.models.Coordinate;
import ch.uetlib.geo.coordinate.util.JtsUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @param origin Origin for all zoom levels
 * @param zoomLevels Definition of all available zoom levels
 */
public record TileLevelsDefinition(Coordinate origin, List<ZoomLevel> zoomLevels) {

    public TileLevelsDefinition(
            final Coordinate origin, final @Nullable List<ZoomLevel> zoomLevels) {
        if (zoomLevels == null || zoomLevels.isEmpty()) {
            throw new IllegalArgumentException("Zoom levels are not allowed to be null or empty");
        }
        this.origin = origin;
        this.zoomLevels =
                zoomLevels.stream()
                        .distinct()
                        .sorted(Comparator.comparingInt(ZoomLevel::level))
                        .toList();
    }

    private static boolean isDefined(final ZoomLevel zoomLevel, final TileInfo tileInfo) {
        return tileInfo.x() >= 0
                && tileInfo.y() >= 0
                && tileInfo.x() < zoomLevel.tilesX()
                && tileInfo.y() < zoomLevel.tilesY();
    }

    public ZoomLevel minZoomLevel() {
        return zoomLevels.getFirst();
    }

    public ZoomLevel maxZoomLevel() {
        return zoomLevels.getLast();
    }

    public int minLevel() {
        return minZoomLevel().level();
    }

    public int maxLevel() {
        return maxZoomLevel().level();
    }

    public Optional<ZoomLevel> nextSmaller(final ZoomLevel level) {
        final var index = zoomLevels.indexOf(level) - 1;
        if (index < 0) {
            return Optional.empty();
        }

        return Optional.of(zoomLevels.get(index));
    }

    public Optional<ZoomLevel> nextLarger(final ZoomLevel level) {
        final var index = zoomLevels.indexOf(level) + 1;
        if (index >= zoomLevels.size()) {
            return Optional.empty();
        }

        return Optional.of(zoomLevels.get(index));
    }

    public TileLevelsDefinition withMaxLevel(final int maxLevel) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> z.level() <= maxLevel).toList());
    }

    public TileLevelsDefinition withMinLevel(final int minLevel) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> z.level() >= minLevel).toList());
    }

    public boolean isMin(final ZoomLevel zoomLevel) {
        return zoomLevel.equals(minZoomLevel());
    }

    public boolean isMax(final ZoomLevel zoomLevel) {
        return zoomLevel.equals(maxZoomLevel());
    }

    public TileLevelsDefinition onlyWithLevels(final Integer... levels) {
        return onlyWithLevels(List.of(levels));
    }

    public TileLevelsDefinition onlyWithLevels(final Collection<Integer> levels) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> levels.contains(z.level())).toList());
    }

    public TileLevelsDefinition addLevel(final ZoomLevel zoomLevel) {
        final var combinedLevels = new ArrayList<>(zoomLevels);
        combinedLevels.add(zoomLevel);
        return new TileLevelsDefinition(origin, combinedLevels);
    }

    public TileLevelsDefinition withMinMaxLevel(final int minLevel, final int maxLevel) {
        return new TileLevelsDefinition(
                origin,
                zoomLevels.stream()
                        .filter(z -> z.level() >= minLevel && z.level() <= maxLevel)
                        .toList());
    }

    public long numberOfTiles() {
        return zoomLevels.stream().mapToLong(ZoomLevel::numberOfTiles).sum();
    }

    public ReferencedEnvelope envelopeOf(final ZoomLevel level, final int x, final int y) {
        final var crs = origin.crs();
        final var originJts = origin.toJts();

        // We expect the coordinate system to be oriented (east, north)
        JtsUtil.throwIfCoordinateSystemNotEastNorth(crs);

        final double tileWidth = level.tileWidth();
        final double tileHeight = level.tileHeight();

        final var xMin = originJts.x + x * tileWidth;
        final var xMax = xMin + tileWidth;

        // The tile origin is top left, the coordinate system increases in y from bottom to top
        final var yMax = originJts.y - y * tileHeight;
        final var yMin = yMax - tileHeight;

        return new ReferencedEnvelope(new Envelope(xMin, xMax, yMin, yMax), crs);
    }

    /** Return given zoom level object */
    public ZoomLevel getZoomLevel(final int level) {
        return getZoomLevelOptional(level).orElseThrow();
    }

    public Optional<ZoomLevel> getZoomLevelOptional(final int level) {
        return zoomLevels.stream().filter(zoomLevel -> zoomLevel.level() == level).findAny();
    }

    /**
     * Returns a collection of all tile infos of the next larger level (larger level = smaller area
     * covered) that overlap with the provided TileInfo
     */
    public Set<TileInfo> childrenTileInfos(final TileInfo current) {
        final var currentLevel = current.zoomLevel();
        return overlappingTileInfos(current, nextLarger(currentLevel).orElse(null));
    }

    /**
     * Returns a collection of all tile infos of the next smaller level (smaller level = larger area
     * covered) that overlap with the provided TileInfo
     */
    public Set<TileInfo> parentTileInfos(final TileInfo current) {
        final var currentLevel = current.zoomLevel();
        return overlappingTileInfos(current, nextSmaller(currentLevel).orElse(null));
    }

    /**
     * Returns a collection of all tile infos at level otherLevel that overlap with the given
     * TileInfo
     */
    private Set<TileInfo> overlappingTileInfos(
            final TileInfo current, final @Nullable ZoomLevel otherLevel) {
        if (otherLevel == null) {
            return Set.of();
        }

        return tileInfos(otherLevel.level(), current.envelope())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Stream<TileInfo> tileInfos(final Collection<Integer> levels) {
        return tileInfos(levels.stream().mapToInt(i -> i));
    }

    public Stream<TileInfo> tileInfos(final IntStream levels) {
        return levels.boxed().flatMap(this::tileInfos);
    }

    public Stream<TileInfo> tileInfos(
            final Collection<Integer> levels, final ReferencedEnvelope outputEnvelope) {
        return tileInfos(levels.stream().mapToInt(i -> i), outputEnvelope);
    }

    public Stream<TileInfo> tileInfos(
            final Collection<Integer> levels,
            final Collection<ReferencedEnvelope> outputEnvelopes) {
        return tileInfos(levels.stream().mapToInt(i -> i), outputEnvelopes);
    }

    public Stream<TileInfo> tileInfos(
            final IntStream levels, final ReferencedEnvelope outputEnvelope) {
        return levels.boxed().flatMap(level -> tileInfos(level, outputEnvelope));
    }

    public Stream<TileInfo> tileInfos(
            final IntStream levels, final Collection<ReferencedEnvelope> outputEnvelopes) {
        return levels.boxed().flatMap(level -> tileInfos(level, outputEnvelopes));
    }

    public Stream<TileInfo> tileInfos(final int level) {
        final var zoomLevel = getZoomLevel(level);
        return IntStream.range(0, zoomLevel.tilesX())
                .boxed()
                .flatMap(
                        x ->
                                IntStream.range(0, zoomLevel.tilesY())
                                        .mapToObj(y -> tileInfo(x, y, level)));
    }

    public Stream<TileInfo> tileInfos(
            final int level, final @Nullable Collection<ReferencedEnvelope> outputEnvelopes) {
        if (CollectionUtils.isEmpty(outputEnvelopes)) {
            return tileInfos(level);
        }

        return outputEnvelopes.stream().flatMap(envelope -> tileInfos(level, envelope)).distinct();
    }

    public Stream<TileInfo> tileInfos(
            final int level, final @Nullable ReferencedEnvelope outputEnvelope) {
        if (outputEnvelope == null) {
            return tileInfos(level);
        }

        // Transform envelope to current crs if needed
        final var transformedEnvelope = JtsUtil.transformEnvelope(outputEnvelope, origin().crs());

        final var zoomLevel = getZoomLevel(level);

        return findXRangeForZoomLevel(zoomLevel, transformedEnvelope)
                .boxed()
                .flatMap(
                        x ->
                                findYRangeForZoomLevel(zoomLevel, transformedEnvelope)
                                        .mapToObj(y -> tileInfo(x, y, level)))
                .filter(tileInfo -> isDefined(zoomLevel, tileInfo));
    }

    private IntStream findXRangeForZoomLevel(final ZoomLevel zoomLevel, final Envelope envelope) {
        final var minX = (envelope.getMinX() - origin.toJts().x) / zoomLevel.tileWidth();
        final var maxX = (envelope.getMaxX() - origin.toJts().x) / zoomLevel.tileWidth();

        return IntervalUtils.intervalToIntRange(minX, maxX);
    }

    private IntStream findYRangeForZoomLevel(final ZoomLevel zoomLevel, final Envelope envelope) {
        // Inverted axis. Envelope coordinate system increases north, tile info downwards.
        final var minY = (origin.toJts().y - envelope.getMaxY()) / zoomLevel.tileHeight();
        final var maxY = (origin.toJts().y - envelope.getMinY()) / zoomLevel.tileHeight();

        return IntervalUtils.intervalToIntRange(minY, maxY);
    }

    public TileInfo tileInfo(final int x, final int y, final int level) {
        return new TileInfo(x, y, getZoomLevel(level), this);
    }
}
