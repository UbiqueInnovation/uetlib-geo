package ch.ubique.uetlib.geo.tile.vector.utils;

import ch.ubique.uetlib.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileElement;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileElementWithMetadata;
import ch.ubique.uetlib.geo.tile.vector.models.VectorTileElementWithTags;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            tags.put("metadata", objectMapper.writeValueAsString(withMetadata.metadata()));
        }

        return tags;
    }
}
