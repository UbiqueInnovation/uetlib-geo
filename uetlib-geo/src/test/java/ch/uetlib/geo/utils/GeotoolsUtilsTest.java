package ch.uetlib.geo.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

class GeotoolsUtilsTest {

    @Test
    void streamReaderClosesReaderOnStreamClose() throws IOException {
        final var reader = mock(SimpleFeatureReader.class);
        when(reader.hasNext()).thenReturn(false);

        try (final var stream = GeotoolsUtils.stream(reader)) {
            assertThat(stream.toList()).isEmpty();
        }

        verify(reader).close();
    }

    @Test
    void streamReaderWrapsCloseException() throws IOException {
        final var reader = mock(SimpleFeatureReader.class);
        when(reader.hasNext()).thenReturn(false);
        final var closeException = new IOException("close failed");
        org.mockito.Mockito.doThrow(closeException).when(reader).close();
        final var stream = GeotoolsUtils.stream(reader);

        assertThatThrownBy(stream::close)
                .isInstanceOf(UncheckedIOException.class)
                .hasCause(closeException);
    }

    @Test
    void withReaderClosesReader() throws IOException {
        final var reader = mock(SimpleFeatureReader.class);
        when(reader.hasNext()).thenReturn(false);

        final var features = GeotoolsUtils.withReader(reader, Stream::toList);

        assertThat(features).isEmpty();
        verify(reader).close();
    }

    @Test
    void withIteratorClosesIterator() {
        final var iterator = mock(SimpleFeatureIterator.class);
        when(iterator.hasNext()).thenReturn(false);

        final var features = GeotoolsUtils.withIterator(iterator, Stream::toList);

        assertThat(features).isEmpty();
        verify(iterator).close();
    }
}
