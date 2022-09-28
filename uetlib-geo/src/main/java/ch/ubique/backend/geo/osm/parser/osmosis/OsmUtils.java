package ch.ubique.backend.geo.osm.parser.osmosis;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import org.apache.commons.lang3.LocaleUtils;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OsmUtils {

    private OsmUtils() {}

    public static Map<String, String> getTags(final Entity entity) {
        return entity.getTags().stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    public static Map<String, String> getLocalized(final Entity entity, final String tagName) {
        return entity.getTags().stream()
                .filter(tag -> tag.getKey().startsWith(tagName + ":"))
                .map(tag -> Map.entry(getLocale(tag.getKey().split(":")[1]), tag.getValue()))
                .filter(tag -> tag.getKey().isPresent())
                .map(tag -> Map.entry(tag.getKey().get().toString(), tag.getValue()))
                .collect(Collectors.groupingBy(Map.Entry::getKey))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue()));
    }

    private static Optional<Locale> getLocale(String key) {
        try {
            final var locale = LocaleUtils.toLocale(key);
            return LocaleUtils.isAvailableLocale(locale) ? Optional.of(locale) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static WGS84Coordinate getCoordinate(final Node node) {
        final var tags = getTags(node);
        final var elevationTagValue = tags.get("ele");
        Double elevation = null;
        if (elevationTagValue != null) {
            try {
                elevation = Double.parseDouble(elevationTagValue);
            } catch (NumberFormatException e) { /* Nothing to do here*/}
        }
        return new WGS84Coordinate(node.getLatitude(), node.getLongitude(), elevation);
    }

    public static <T> Collection<T> parseNodes(final InputStream inputStream, final Function<Node, Optional<T>> nodeMapper) {
        final var results = new ArrayList<T>();
        OsmParser.from(inputStream)
                .onNode(node -> {
                    final var result = nodeMapper.apply(node);
                    result.ifPresent(results::add);
                })
                .parse();
        return results;
    }
}
