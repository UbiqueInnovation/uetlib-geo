package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithMetadata;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElementWithTags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VectorTileElementUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper(); // TODO pass as parameter
    private static final Logger logger = LoggerFactory.getLogger(VectorTileElementUtils.class);

    private VectorTileElementUtils() {}

    public static Map<String, Object> getFinalTags(final VectorTileElement element) {

        final var tags = new HashMap<String, Object>();

        if (element instanceof VectorTileElementWithTags<?> withTags && withTags.tags() != null) {
            //noinspection unchecked
            tags.putAll(objectMapper.convertValue(withTags.tags(), Map.class));
        }

        if (element instanceof VectorTileElementWithMetadata<?> withMetadata
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
