package ch.ubique.backend.geo.tile.vector.mapbox.filter.definition;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.FilterDefinitionBuilder;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.FilterRuleBuilder;

import java.util.List;

public record FilterDefinition(List<FilterRule> rules) {

    public static FilterDefinitionBuilder withRules(final FilterRuleBuilder... ruleBuilders) {
        return new FilterDefinitionBuilder().addAll(List.of(ruleBuilders));
    }
}
