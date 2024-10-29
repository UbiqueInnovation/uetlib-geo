package ch.ubique.backend.geo.tile.vector.mapbox.filter.definition;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.ExcludeRuleBuilder;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.IncludeRuleBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
public record FilterRule(
        @NotNull FilterRuleType type,
        @NotNull Collection<String> layers,
        @NotNull Map<String, Object> tags,
        @Nullable TagFilterRule tagRule) {

    public FilterRule {
        layers = Objects.requireNonNullElseGet(layers, List::of);
        tags = Objects.requireNonNullElseGet(tags, Map::of);
    }

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
