package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.builder;

import ch.ubique.backend.geo.tile.vector.mapbox.tilejson.TileJsonVectorLayer;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TileJsonVectorLayerBuilder {
    private final String id;
    private @Nullable String description;
    private @Nullable Integer minZoom;
    private @Nullable Integer maxZoom;
    private final Map<String, TileJsonVectorLayerFieldBuilder> fieldValuesBuilder =
            new ConcurrentHashMap<>();

    public TileJsonVectorLayerBuilder(final String id) {
        this.id = id;
    }

    public TileJsonVectorLayerBuilder addLevel(final int level) {
        if (minZoom == null) {
            minZoom = level;
        }
        if (maxZoom == null) {
            maxZoom = level;
        }
        minZoom = Math.min(minZoom, level);
        maxZoom = Math.max(maxZoom, level);
        return this;
    }

    public TileJsonVectorLayerBuilder addField(final String key, final Object value) {
        fieldValuesBuilder
                .computeIfAbsent(key, k -> new TileJsonVectorLayerFieldBuilder())
                .add(value);
        return this;
    }

    public TileJsonVectorLayerBuilder description(final String description) {
        this.description = description;
        return this;
    }

    public TileJsonVectorLayer build() {
        if (StringUtils.isBlank(id)) {
            throw new IllegalStateException("id must not be blank");
        }
        final var fields =
                fieldValuesBuilder.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().build()));
        return new TileJsonVectorLayer(id, description, minZoom, maxZoom, fields);
    }
}
