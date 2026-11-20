package ch.uetlib.geo.tile.vector.mapbox.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.uetlib.geo.tile.vector.mapbox.filter.definition.FilterDefinition;
import ch.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRule;
import ch.uetlib.geo.tile.vector.mapbox.filter.definition.FilterRuleType;
import ch.uetlib.geo.tile.vector.mapbox.models.VectorTileFeature;
import ch.uetlib.geo.tile.vector.mapbox.models.VectorTileLayer;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VectorTileFilterTagMatchTest {

    private static final VectorTileLayer LAYER = new VectorTileLayer(4096, "roads", List.of());
    private static final VectorTileFilter FILTER =
            new VectorTileFilter(FilterDefinition.withRules().includeOthers());

    private static VectorTileFeature feature(final Map<String, Object> tags) {
        return new VectorTileFeature(new GeometryFactory().createPoint(new Coordinate(1, 2)), tags);
    }

    private static FilterRule ruleWithTag(final String key, final Object value) {
        final var tags = new HashMap<String, Object>();
        tags.put(key, value);
        return new FilterRule(FilterRuleType.INCLUDE, List.of(), tags, null);
    }

    @Test
    void nullTagValueMatchesOnlyAMissingTag() {
        final var rule = ruleWithTag("name", null);

        assertTrue(FILTER.ruleMatches(rule, feature(Map.of()), LAYER));
        assertFalse(FILTER.ruleMatches(rule, feature(Map.of("name", "A1")), LAYER));
    }

    @Test
    void numbersMatchAcrossTypesAndOtherValuesByEquals() {
        assertTrue(
                FILTER.ruleMatches(ruleWithTag("level", 2), feature(Map.of("level", 2L)), LAYER));
        assertFalse(
                FILTER.ruleMatches(ruleWithTag("level", 2), feature(Map.of("level", 2.5)), LAYER));
        assertTrue(
                FILTER.ruleMatches(
                        ruleWithTag("kind", "road"), feature(Map.of("kind", "road")), LAYER));
        assertFalse(FILTER.ruleMatches(ruleWithTag("kind", "road"), feature(Map.of()), LAYER));
    }
}
