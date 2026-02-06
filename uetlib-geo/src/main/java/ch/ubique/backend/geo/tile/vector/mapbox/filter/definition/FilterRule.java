package ch.ubique.backend.geo.tile.vector.mapbox.filter.definition;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.ExcludeRuleBuilder;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.IncludeRuleBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public record FilterRule(
        FilterRuleType type,
        Collection<String> layers,
        Map<String, Object> tags,
        @Nullable TagFilterRule tagRule) {

    public static IncludeRuleBuilder include() {
        return new IncludeRuleBuilder();
    }

    public static ExcludeRuleBuilder exclude() {
        return new ExcludeRuleBuilder();
    }

    public static FilterRule includeAll() {
        return include().build();
    }

    public static FilterRule excludeAll() {
        return exclude().build();
    }
}
