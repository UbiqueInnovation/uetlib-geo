package ch.ubique.backend.geo.tile.vector.mapbox.tilejson.builder;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TileJsonVectorLayerFieldBuilder {

    private final Collection<Object> values = ConcurrentHashMap.newKeySet();

    public TileJsonVectorLayerFieldBuilder add(final Object value) {
        values.add(value);
        return this;
    }

    public String build() {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalStateException("values must not be empty");
        }
        if (values.size() == 1) {
            return values.iterator().next().toString();
        }
        if (values.stream().allMatch(Boolean.class::isInstance)) {
            return "Boolean";
        }
        if (values.size() <= 3) {
            return "One of: "
                    + values.stream()
                            .map(Object::toString)
                            .sorted()
                            .collect(Collectors.joining(", "));
        }
        if (values.stream().allMatch(String.class::isInstance)) {
            return "Number";
        }
        if (values.stream().allMatch(Number.class::isInstance)) {
            return "Number";
        }
        return "Any";
    }
}
