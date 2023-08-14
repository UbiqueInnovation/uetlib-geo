package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithMetadata;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VectorTileElementUtils {

    private static final Logger logger = LoggerFactory.getLogger(VectorTileElementUtils.class);

    private VectorTileElementUtils() {}

    public static Map<String, Object> getFinalTags(
            final VectorTileElement element, final VectorTileConfig config) {

        final var tags = new HashMap<String, Object>();
        final var objectMapper = config.objectMapper();

        if (element instanceof final VectorTileElementWithTags<?> withTags
                && withTags.tags() != null) {
            //noinspection unchecked
            tags.putAll(objectMapper.convertValue(withTags.tags(), Map.class));
        }

        if (element instanceof final VectorTileElementWithMetadata<?> withMetadata
                && withMetadata.metadata() != null) {
            try {
                tags.put("metadata", objectMapper.writeValueAsString(withMetadata.metadata()));
            } catch (final JsonProcessingException e) {
                logger.error("Could not serialize metadata to JSON");
            }
        }

        return tags;
    }
}
