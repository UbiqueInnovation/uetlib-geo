package ch.uetlib.geo.tile.vector.mapbox.filter.definition.builder;

import ch.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRule;
import ch.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRuleType;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExcludeRuleBuilder implements FilterRuleBuilder {

    protected @Nullable Collection<String> layers;
    protected @Nullable Map<String, Object> tags;

    public ExcludeRuleBuilder layer(final String layer) {
        return layers(layer);
    }

    public ExcludeRuleBuilder layers(final String... layer) {
        return layers(List.of(layer));
    }

    public ExcludeRuleBuilder layers(final Collection<String> layers) {
        this.layers = layers;
        return this;
    }

    public ExcludeRuleBuilder tags(final Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    public ExcludeRuleBuilder tag(final String key, final Object value) {
        return tags(Map.of(key, value));
    }

    @Override
    public FilterRule build() {
        return new FilterRule(
                FilterRuleType.EXCLUDE,
                Objects.requireNonNullElseGet(layers, List::of),
                Objects.requireNonNullElseGet(tags, Map::of),
                null);
    }
}
