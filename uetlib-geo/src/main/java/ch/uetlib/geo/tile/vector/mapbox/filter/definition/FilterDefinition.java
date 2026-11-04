package ch.uetlib.geo.tile.vector.mapbox.filter.definition;

import ch.uetlib.geo.tile.vector.mapbox.filter.definition.builder.FilterDefinitionBuilder;
import ch.uetlib.geo.tile.vector.mapbox.filter.definition.builder.FilterRuleBuilder;

import java.util.List;

public record FilterDefinition(List<FilterRule> rules) {

    public static FilterDefinitionBuilder withRules(final FilterRuleBuilder... ruleBuilders) {
        return new FilterDefinitionBuilder().addAll(List.of(ruleBuilders));
    }
}
