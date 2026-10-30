package ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.builder;

import ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.FilterDefinition;
import ch.ubique.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FilterDefinitionBuilder {

    private final List<FilterRule> rules;

    public FilterDefinitionBuilder() {
        this.rules = new ArrayList<>();
    }

    public FilterDefinitionBuilder addAll(final Collection<FilterRuleBuilder> ruleBuilders) {
        ruleBuilders.forEach(this::add);
        return this;
    }

    public FilterDefinitionBuilder add(final FilterRuleBuilder ruleBuilder) {
        rules.add(ruleBuilder.build());
        return this;
    }

    public FilterDefinition includeOthers() {
        rules.add(FilterRule.includeAll());
        return new FilterDefinition(rules);
    }

    public FilterDefinition excludeOthers() {
        rules.add(FilterRule.excludeAll());
        return new FilterDefinition(rules);
    }
}
