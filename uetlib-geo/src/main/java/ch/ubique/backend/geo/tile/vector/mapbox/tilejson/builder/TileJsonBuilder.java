package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.builder;

import ch.ubique.backend.geo.coordinate.models.BoundingBox;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJson;
import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJsonCenter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullUnmarked
public class TileJsonBuilder {

    private List<String> tiles;
    private String attribution;
    private BoundingBox<@NonNull WGS84Coordinate> bounds;
    private TileJsonCenter center;
    private List<String> data;
    private String description;
    private Integer fillZoom;
    private List<String> grids;
    private String legend;
    private String name;
    private String scheme;
    private String template;
    private String version;

    private final Collection<Integer> zoomLevels = ConcurrentHashMap.newKeySet();
    private final Map<String, Object> additionalData = new ConcurrentHashMap<>();
    private final Map<String, TileJsonVectorLayerBuilder> vectorLayerBuilders =
            new ConcurrentHashMap<>();

    public TileJsonBuilder tiles(@NotBlank final String tiles) {
        this.tiles = List.of(tiles);
        return this;
    }

    public TileJsonBuilder tiles(@NotEmpty final Collection<String> tiles) {
        this.tiles = tiles.stream().toList();
        return this;
    }

    public TileJsonVectorLayerBuilder vectorLayerBuilder(final String layerName) {
        return vectorLayerBuilders.computeIfAbsent(layerName, TileJsonVectorLayerBuilder::new);
    }

    public TileJsonBuilder attribution(@Nullable final String attribution) {
        this.attribution = attribution;
        return this;
    }

    public TileJsonBuilder bounds(@Nullable final BoundingBox<@NonNull WGS84Coordinate> bounds) {
        this.bounds = bounds;
        return this;
    }

    public TileJsonBuilder center(@Nullable final TileJsonCenter center) {
        this.center = center;
        return this;
    }

    public TileJsonBuilder data(@Nullable final List<String> data) {
        this.data = data;
        return this;
    }

    public TileJsonBuilder description(@Nullable final String description) {
        this.description = description;
        return this;
    }

    public TileJsonBuilder fillZoom(@Nullable final Integer fillZoom) {
        this.fillZoom = fillZoom;
        return this;
    }

    public TileJsonBuilder grids(@Nullable final List<String> grids) {
        this.grids = grids;
        return this;
    }

    public TileJsonBuilder legend(@Nullable final String legend) {
        this.legend = legend;
        return this;
    }

    public TileJsonBuilder maxZoom(@Nullable final Integer maxZoom) {
        zoomLevels.add(maxZoom);
        return this;
    }

    public TileJsonBuilder minZoom(@Nullable final Integer minZoom) {
        zoomLevels.add(minZoom);
        return this;
    }

    public TileJsonBuilder name(@Nullable final String name) {
        this.name = name;
        return this;
    }

    public TileJsonBuilder scheme(@Nullable final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public TileJsonBuilder template(@Nullable final String template) {
        this.template = template;
        return this;
    }

    public TileJsonBuilder version(@Nullable final String version) {
        this.version = version;
        return this;
    }

    public TileJsonBuilder additionalData(final String key, final Object value) {
        additionalData.put(key, value);
        return this;
    }

    public TileJsonBuilder addLevel(final int level) {
        zoomLevels.add(level);
        return this;
    }

    public TileJsonBuilder additionalData(final Map<String, Object> additionalData) {
        this.additionalData.putAll(additionalData);
        return this;
    }

    public TileJson build() {
        if (CollectionUtils.isEmpty(tiles)) {
            throw new IllegalStateException("tiles must not be null or empty");
        }
        final var vectorLayers =
                vectorLayerBuilders.values().stream()
                        .map(TileJsonVectorLayerBuilder::build)
                        .toList();
        return new TileJson(
                "3.0.0",
                tiles,
                vectorLayers,
                attribution,
                bounds,
                center,
                data,
                description,
                fillZoom,
                grids,
                legend,
                zoomLevels.stream().max(Comparator.naturalOrder()).orElse(null),
                zoomLevels.stream().min(Comparator.naturalOrder()).orElse(null),
                name,
                scheme,
                template,
                version,
                additionalData);
    }
}
