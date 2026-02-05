package ch.ubique.backend.geo.tile.vector.mapbox.filter;

import static java.util.Objects.requireNonNullElseGet;

import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.FilterDefinition;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.FilterRule;
import ch.ubique.backend.geo.tile.vector.mapbox.filter.definition.TagFilterRule;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileFeature;
import ch.ubique.backend.geo.tile.vector.mapbox.models.VectorTileLayer;

import org.jspecify.annotations.Nullable;

import java.util.Map.Entry;
import java.util.Objects;

public class VectorTileFilter {

    private final FilterDefinition filterDefinition;

    public VectorTileFilter(final FilterDefinition filterDefinition) {
        this.filterDefinition = filterDefinition;
    }

    public VectorTile filter(final VectorTile vectorTile) {
        return new VectorTile(
                vectorTile.layers().stream().map(this::filter).filter(Objects::nonNull).toList());
    }

    @Nullable
    private VectorTileLayer filter(final VectorTileLayer layer) {
        final var features =
                layer.features().stream()
                        .map(feature -> filter(feature, layer))
                        .filter(Objects::nonNull)
                        .toList();

        if (features.isEmpty()) {
            return null;
        }
        return layer.withFeatures(features);
    }

    @Nullable
    private VectorTileFeature filter(final VectorTileFeature feature, final VectorTileLayer layer) {
        final var rule =
                filterDefinition.rules().stream()
                        .filter(r -> ruleMatches(r, feature, layer))
                        .findFirst()
                        .orElseThrow(() -> throwNoMatchingRule(feature, layer));

        return switch (rule.type()) {
            case INCLUDE -> {
                final var tagRule = requireNonNullElseGet(rule.tagRule(), TagFilterRule::keepAll);
                yield feature.withTags(tagRule.filterTags(feature.tags()));
            }
            case EXCLUDE -> null;
        };
    }

    public boolean ruleMatches(
            final FilterRule rule, final VectorTileFeature feature, final VectorTileLayer layer) {
        final var layerMatches = rule.layers().isEmpty() || rule.layers().contains(layer.name());
        final var tagsMatch =
                rule.tags().entrySet().stream().allMatch(entry -> tagEntryMatches(feature, entry));
        return layerMatches && tagsMatch;
    }

    private static boolean tagEntryMatches(
            final VectorTileFeature feature, final Entry<String, Object> entry) {
        final var featureValue = feature.tags().get(entry.getKey());
        final var filterValue = entry.getValue();
        if (filterValue == featureValue) {
            return true;
        }
        if (filterValue == null) {
            return false;
        }
        if (filterValue instanceof final Number a && featureValue instanceof final Number b) {
            return a.doubleValue() == b.doubleValue() && a.longValue() == b.longValue();
        }
        return Objects.equals(featureValue, filterValue);
    }

    private static RuntimeException throwNoMatchingRule(
            final VectorTileFeature feature, final VectorTileLayer layer) {
        return new IllegalStateException(
                "No matching rule found. "
                        + "Consider adding a wildcard isInclude or exclude as last rule. "
                        + "Layer: "
                        + layer.name()
                        + ", Feature: "
                        + feature);
    }
}
