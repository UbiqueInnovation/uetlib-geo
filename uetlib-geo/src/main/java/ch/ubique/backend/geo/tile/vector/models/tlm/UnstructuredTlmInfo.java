package ch.ubique.backend.geo.tile.vector.models.tlm;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public record UnstructuredTlmInfo(Map<String, Object> properties) implements TlmInfo {

    @Override
    public UnstructuredTlmInfo unstructured() {
        return this;
    }

    private static final Collection<String> NULL_VALUES = List.of("k_W", "ub");
    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZONE);

    public Optional<String> getString(final String key) {
        if (!properties.containsKey(key)) {
            return Optional.empty();
        }

        final var value = properties.get(key).toString();
        if (StringUtils.isBlank(value) || NULL_VALUES.contains(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public <E extends Enum<E>> Optional<E> getEnum(final Class<E> enumClass, final String key) {
        return getString(key)
                .map(value -> EnumUtils.getEnumIgnoreCase(enumClass, value.replace(' ', '_')));
    }

    public Optional<Integer> getInt(final String key) {
        return getString(key).map(UnstructuredTlmInfo::parseInt);
    }

    public Optional<Instant> getInstant(final String key) {
        return getString(key).map(UnstructuredTlmInfo::parseInstant);
    }

    public Optional<Instant> getInstant(final String yearKey, final String monthKey) {
        return getInt(yearKey)
                .flatMap(year -> getInt(monthKey).map(month -> toInstant(year, month)));
    }

    public Optional<Boolean> getBoolean(final String key) {
        return getString(key).map(UnstructuredTlmInfo::parseBoolean);
    }

    private static Boolean parseBoolean(final String value) {
        return switch (value.toLowerCase()) {
            case "wahr" -> true;
            case "falsch" -> false;
            default ->
                    throw new IllegalStateException(
                            "Unexpected boolean value: " + value.toLowerCase());
        };
    }

    private static Instant parseInstant(final String value) {
        return LocalDate.from(DATE_TIME_FORMATTER.parse(value))
                .atTime(LocalTime.MIN)
                .atZone(ZONE)
                .toInstant();
    }

    private static int parseInt(final String value) {
        final var relevantPart = List.of(value.split("_")).getLast(); // Remove prefix
        final var intValue = Integer.parseInt(relevantPart);
        if (value.contains("minus")) {
            return -intValue;
        }
        return intValue;
    }

    private static Instant toInstant(final Integer year, final Integer month) {
        return LocalDate.of(year, month, 1).atTime(LocalTime.MIN).atZone(ZONE).toInstant();
    }
}
