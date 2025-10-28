package ch.ubique.backend.geo.kml.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

class KmlParserTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/kml/sample.kml.gz",
                "/kml/sample2.kml.gz",
                "/kml/lt.kml.gz",
                "/kml/macras.kml.gz",
                "/kml/closed-trails.kml.gz",
                "/kml/active-obstacles.kml.gz",
                "/kml/polygon_with_hole.kml.gz"
            })
    void testParseKml(final String resourcePath) throws IOException {
        try (final var inputStream =
                new GZIPInputStream(
                        Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)))) {
            final var document = assertDoesNotThrow(() -> KmlParser.parse(inputStream));
            assertNotNull(document);
            assertFalse(document.getPlacemarks().isEmpty());
            assertNotNull(document.getStyles());
            assertNotNull(document.getStyleMaps());
            for (final var placemark : document.getPlacemarks()) {
                assertNotNull(placemark);
                assertNotNull(placemark.getGeometry());
                assertFalse(placemark.getGeometry().isEmpty(), placemark.getName() + " is empty.");
                assertTrue(placemark.getGeometry().isValid());
            }
        }
    }
}
