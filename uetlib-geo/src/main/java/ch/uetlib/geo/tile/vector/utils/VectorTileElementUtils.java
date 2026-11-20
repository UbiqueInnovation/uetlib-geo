package ch.uetlib.geo.tile.vector.utils;

import ch.uetlib.geo.tile.vector.models.VectorTileConfig;
import ch.uetlib.geo.tile.vector.models.VectorTileElement;
import ch.uetlib.geo.tile.vector.models.VectorTileElementWithMetadata;
import ch.uetlib.geo.tile.vector.models.VectorTileElementWithTags;

import java.util.HashMap;
import java.util.Map;

public final class VectorTileElementUtils {

    private VectorTileElementUtils() {}

    public static Map<String, Object> getFinalTags(
            final VectorTileElement element, final VectorTileConfig config) {

        final var tags = new HashMap<String, Object>();
        final var objectMapper = config.objectMapper();

        if (element instanceof final VectorTileElementWithTags<?> withTags
                && withTags.tags() != null) {
            tags.putAll(
                    objectMapper.convertValue(
                            withTags.tags(),
                            objectMapper
                                    .getTypeFactory()
                                    .constructParametricType(
                                            Map.class, String.class, Object.class)));
        }

        if (element instanceof final VectorTileElementWithMetadata<?> withMetadata
                && withMetadata.metadata() != null) {
            tags.put("metadata", objectMapper.writeValueAsString(withMetadata.metadata()));
        }

        return tags;
    }
}
