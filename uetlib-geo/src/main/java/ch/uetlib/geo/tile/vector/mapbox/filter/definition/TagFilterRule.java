package ch.uetlib.geo.tile.vector.mapbox.filter.definition;

import ch.uetlib.core.utils.MapCollectors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record TagFilterRule(boolean keep, Collection<String> tags) {

    public static TagFilterRule keep(final Collection<String> tags) {
        return new TagFilterRule(true, tags);
    }

    public static TagFilterRule delete(final Collection<String> tags) {
        return new TagFilterRule(false, tags);
    }

    public static TagFilterRule keepAll() {
        // Delete all tags that are in the empty list => keeps all
        return delete(List.of());
    }

    public static TagFilterRule deleteAll() {
        // Keeps all tags that are in the empty list => keeps all
        return keep(List.of());
    }

    public Map<String, Object> filterTags(final Map<String, Object> tagsToFilter) {
        return tagsToFilter.entrySet().stream()
                .filter(entry -> tags.contains(entry.getKey()) == keep)
                .collect(MapCollectors.entriesToMap());
    }
}
