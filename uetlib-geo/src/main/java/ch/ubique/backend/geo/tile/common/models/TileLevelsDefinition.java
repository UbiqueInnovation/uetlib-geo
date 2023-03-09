package ch.ubique.backend.geo.tile.common.models;

import ch.ubique.backend.geo.coordinate.models.Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;

import jakarta.validation.constraints.NotNull;

import org.eclipse.emf.common.util.WrappedException;
import org.geotools.geometry.jts.ReferencedEnvelope;
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

    public TileLevelsDefinition(Coordinate origin, List<ZoomLevel> zoomLevels) {
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

    public TileLevelsDefinition withMaxLevel(int maxLevel) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> z.level() <= maxLevel).toList());
    }

    public TileLevelsDefinition withMinLevel(int minLevel) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> z.level() >= minLevel).toList());
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

    public TileLevelsDefinition onlyWithLevels(Collection<Integer> levels) {
        return new TileLevelsDefinition(
                origin, zoomLevels.stream().filter(z -> levels.contains(z.level())).toList());
    }

    public TileLevelsDefinition addLevel(ZoomLevel zoomLevel) {
        final var combinedLevels = new ArrayList<>(zoomLevels);
        combinedLevels.add(zoomLevel);
        return new TileLevelsDefinition(origin, combinedLevels);
    }

    public TileLevelsDefinition withMinMaxLevel(int minLevel, int maxLevel) {
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
    @NotNull
    public ZoomLevel getZoomLevel(int level) {
        return getZoomLevelOptional(level).orElseThrow();
    }

    public Optional<ZoomLevel> getZoomLevelOptional(int level) {
        return zoomLevels.stream().filter(zoomLevel -> zoomLevel.level() == level).findAny();
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
                                        .mapToObj(y -> new TileInfo(x, y, otherLevel, this)))
                .collect(Collectors.toUnmodifiableSet());
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

    public Stream<TileInfo> tileInfos(final int level, ReferencedEnvelope outputEnvelope) {
        if (outputEnvelope == null) {
            return tileInfos(level);
        }

        if (!outputEnvelope.getCoordinateReferenceSystem().equals(origin().crs())) {
            try {
                outputEnvelope = outputEnvelope.transform(origin().crs(), true);
            } catch (Exception e) {
                // rethrow as runtime exception. no way for caller to handle this
                throw new WrappedException(e);
            }
        }

        final var zoomLevel = getZoomLevel(level);
        final int minX = findMinXforZoomLevel(zoomLevel, outputEnvelope);
        final int maxX = findMaxXforZoomLevel(zoomLevel, outputEnvelope);
        final int minY = findMinYforZoomLevel(zoomLevel, outputEnvelope);
        final int maxY = findMaxYforZoomLevel(zoomLevel, outputEnvelope);

        return IntStream.rangeClosed(minX, maxX)
                .boxed()
                .flatMap(
                        x ->
                                IntStream.rangeClosed(minY, maxY)
                                        .mapToObj(y -> tileInfo(x, y, level)));
    }

    private int findMinXforZoomLevel(ZoomLevel zoomLevel, ReferencedEnvelope outputEnvelope) {
        for (int x = 0; x < zoomLevel.tilesX(); x++) {
            final ReferencedEnvelope tileEnvelope = envelopeOf(zoomLevel, x, 0);
            if (tileEnvelope.getMaxX() > outputEnvelope.getMinX()) {
                return x;
            }
        }
        throw new RuntimeException(
                String.format(
                        "could not find minX for zoom level: %s and outputEnvelope: %s (crs: %s)",
                        zoomLevel, outputEnvelope, origin.crs()));
    }

    private int findMaxXforZoomLevel(ZoomLevel zoomLevel, ReferencedEnvelope outputEnvelope) {
        for (int x = zoomLevel.tilesX() - 1; x >= 0; x--) {
            final ReferencedEnvelope tileEnvelope = envelopeOf(zoomLevel, x, 0);
            if (tileEnvelope.getMinX() < outputEnvelope.getMaxX()) {
                return x;
            }
        }
        throw new RuntimeException(
                String.format(
                        "could not find maxX for zoom level: %s and outputEnvelope: %s (crs: %s)",
                        zoomLevel, outputEnvelope, origin.crs()));
    }

    private int findMinYforZoomLevel(ZoomLevel zoomLevel, ReferencedEnvelope outputEnvelope) {
        for (int y = 0; y < zoomLevel.tilesY(); y++) {
            final ReferencedEnvelope tileEnvelope = envelopeOf(zoomLevel, 0, y);
            if (tileEnvelope.getMinY() < outputEnvelope.getMaxY()) {
                return y;
            }
        }
        throw new RuntimeException(
                String.format(
                        "could not find minY for zoom level: %s and outputEnvelope: %s (crs: %s)",
                        zoomLevel, outputEnvelope, origin.crs()));
    }

    private int findMaxYforZoomLevel(ZoomLevel zoomLevel, ReferencedEnvelope outputEnvelope) {
        for (int y = zoomLevel.tilesY() - 1; y >= 0; y--) {
            final ReferencedEnvelope tileEnvelope = envelopeOf(zoomLevel, 0, y);
            if (tileEnvelope.getMaxY() > outputEnvelope.getMinY()) {
                return y;
            }
        }
        throw new RuntimeException(
                String.format(
                        "could not find maxY for zoom level: %s and outputEnvelope: %s (crs: %s)",
                        zoomLevel, outputEnvelope, origin.crs()));
    }

    public TileInfo tileInfo(final int x, final int y, final int level) {
        return new TileInfo(x, y, getZoomLevel(level), this);
    }
}
