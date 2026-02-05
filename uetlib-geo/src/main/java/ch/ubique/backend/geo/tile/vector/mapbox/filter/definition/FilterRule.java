package ch.ubique.backend.geo.tile.vector.mapbox.filter.definition;

import static java.util.Objects.requireNonNullElseGet;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.ExcludeRuleBuilder;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.builder.IncludeRuleBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public record FilterRule(
        @NonNull FilterRuleType type,
        @NonNull Collection<String> layers,
        @NonNull Map<String, Object> tags,
        @Nullable TagFilterRule tagRule) {

    public FilterRule {
        layers = requireNonNullElseGet(layers, List::of);
        tags = requireNonNullElseGet(tags, Map::of);
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
