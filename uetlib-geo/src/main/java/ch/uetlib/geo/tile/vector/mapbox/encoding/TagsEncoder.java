package ch.uetlib.geo.tile.vector.mapbox.encoding;

import java.util.*;

public class TagsEncoder {

    Map<String, Integer> keyMap = new LinkedHashMap<>();
    Map<Object, Integer> valueMap = new LinkedHashMap<>();

    public Integer indexOfKey(final String key) {
        return keyMap.computeIfAbsent(key, ignored -> keyMap.size());
    }

    public Integer indexOfValue(final Object value) {
        return valueMap.computeIfAbsent(value, ignored -> valueMap.size());
    }

    public Collection<String> keys() {
        return keyMap.keySet();
    }

    public Collection<Object> values() {
        return valueMap.keySet();
    }
}
