package ch.ubique.backend.geo.tile.vector.utils;

import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VectorTileElementUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(VectorTileElementUtils.class);

    private VectorTileElementUtils() {}

    public static <T, M> Map<String, Object> getFinalTags(VectorTileElement<T, M> element) {

        @SuppressWarnings("unchecked")
        final var tags = new HashMap<String, Object>(objectMapper.convertValue(element.getTags(), Map.class));

        final var metadata = element.getMetadata();
        if (metadata != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String metadataJson = objectMapper.writeValueAsString(metadata);
                tags.put("metadata", metadataJson);
            } catch (JsonProcessingException e) {
                logger.error("Could not serialize metadata to JSON");
            }
        }

        return tags;
    }
}
