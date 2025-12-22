package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithMetadata;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

import java.util.HashMap;
import java.util.Map;

public final class VectorTileElementUtils {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileElementUtils.class);

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
            try {
                tags.put("metadata", objectMapper.writeValueAsString(withMetadata.metadata()));
            } catch (final JacksonException e) {
                logger.error("Could not serialize metadata to JSON");
            }
        }

        return tags;
    }
}
