package ch.ubique.backend.geo.utils;

import ch.ubique.backend.core.utils.OptionalUtils;

import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingSupplier;

import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GeotoolsUtils {

    private GeotoolsUtils() {}

    /**
     * Returns a lazy feature stream backed by the given reader.
     *
     * <p>The returned stream must be closed (for example with try-with-resources). Closing the
     * stream closes the underlying reader.
     *
     * <p>Prefer {@link #withReader(SimpleFeatureReader, ThrowingFunction)} if you want lifecycle
     * handling in one place.
     */
    @NonNull
    public static Stream<SimpleFeature> stream(final SimpleFeatureReader featureReader) {
        return OptionalUtils.generateAsLongAsPresent(nextOptional(featureReader).uncheck())
                .onClose(() -> closeUnchecked(featureReader));
    }

    /**
     * Returns a lazy feature stream backed by the given iterator.
     *
     * <p>The returned stream must be closed (for example with try-with-resources). Closing the
     * stream closes the underlying iterator.
     *
     * <p>Prefer {@link #withIterator(SimpleFeatureIterator, Function)} if you want lifecycle
     * handling in one place.
     */
    @NonNull
    public static Stream<SimpleFeature> stream(final SimpleFeatureIterator featureIterator) {
        return OptionalUtils.generateAsLongAsPresent(nextOptional(featureIterator))
                .onClose(featureIterator::close);
    }

    /** Executes a stream pipeline and always closes the reader and stream afterwards. */
    @NonNull
    public static <T> T withReader(
            final SimpleFeatureReader featureReader,
            final ThrowingFunction<Stream<SimpleFeature>, T, IOException> consumer)
            throws IOException {
        try (final var stream = stream(featureReader)) {
            return consumer.apply(stream);
        }
    }

    /** Executes a stream pipeline and always closes the iterator and stream afterwards. */
    @NonNull
    public static <T> T withIterator(
            final SimpleFeatureIterator featureIterator,
            final Function<Stream<SimpleFeature>, T> consumer) {
        try (final var stream = stream(featureIterator)) {
            return consumer.apply(stream);
        }
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

    private static void closeUnchecked(final SimpleFeatureReader featureReader) {
        try {
            featureReader.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
