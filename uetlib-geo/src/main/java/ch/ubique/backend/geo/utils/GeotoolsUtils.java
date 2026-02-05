package ch.ubique.backend.geo.utils;

import ch.ubique.backend.core.utils.OptionalUtils;

import com.pivovarit.function.ThrowingSupplier;

import org.jspecify.annotations.NonNull;

import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GeotoolsUtils {

    private GeotoolsUtils() {}

    @NonNull
    public static Stream<SimpleFeature> stream(final SimpleFeatureReader featureReader) {
        return OptionalUtils.generateAsLongAsPresent(nextOptional(featureReader).uncheck());
    }

    @NonNull
    public static Stream<SimpleFeature> stream(final SimpleFeatureIterator featureIterator) {
        return OptionalUtils.generateAsLongAsPresent(nextOptional(featureIterator));
    }

    @NonNull
    public static ThrowingSupplier<Optional<SimpleFeature>, IOException> nextOptional(
            final SimpleFeatureReader reader) {
        return () -> reader.hasNext() ? Optional.of(reader.next()) : Optional.empty();
    }

    @NonNull
    public static Supplier<Optional<SimpleFeature>> nextOptional(
            final SimpleFeatureIterator iterator) {
        return () -> iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
    }

    @NonNull
    public static Map<String, Object> getProperties(final SimpleFeature feature) {
        return feature.getProperties().stream()
                .filter(f -> f.getValue() != null)
                .collect(Collectors.toMap(f -> f.getName().toString(), Property::getValue));
    }
}
