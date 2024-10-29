package ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.FilterRule;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.FilterRuleType;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.TagFilterRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IncludeRuleBuilder implements FilterRuleBuilder {

    private TagFilterRule tagRule;
    protected Collection<String> layers;
    protected Map<String, Object> tags;

    public IncludeRuleBuilder layer(final String layer) {
        return layers(layer);
    }

    public IncludeRuleBuilder layers(final String... layer) {
        return layers(List.of(layer));
    }

    public IncludeRuleBuilder layers(final Collection<String> layers) {
        this.layers = layers;
        return this;
    }

    public IncludeRuleBuilder tags(final Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    public IncludeRuleBuilder tag(final String key, final Object value) {
        return tags(Map.of(key, value));
    }

    public IncludeRuleBuilder tagRule(final TagFilterRule tagRule) {
        this.tagRule = tagRule;
        return this;
    }

    @Override
    public FilterRule build() {
        return new FilterRule(FilterRuleType.INCLUDE, layers, tags, tagRule);
    }
}
