package ch.uetlib.geo.kml.parser;

import static org.junit.jupiter.api.Assertions.*;

import static java.util.Objects.requireNonNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                new GZIPInputStream(requireNonNull(getClass().getResourceAsStream(resourcePath)))) {
            final var document = assertDoesNotThrow(() -> KmlParser.parse(inputStream));
            assertNotNull(document);
            assertFalse(document.placemarks().isEmpty());
            assertNotNull(document.styles());
            assertNotNull(document.styleMaps());
            for (final var placemark : document.placemarks()) {
                assertNotNull(placemark);
                assertNotNull(placemark.geometry());
                assertFalse(placemark.geometry().isEmpty(), placemark.name() + " is empty.");
                assertTrue(placemark.geometry().isValid());
            }
        }
    }

    @Test
    void preservesStylesAndOptionalPlacemarkProperties() throws IOException {
        final var kml =
                """
                <kml><Document>
                  <Style id="main">
                    <IconStyle><scale>2</scale><Icon><href>icon.png</href></Icon></IconStyle>
                    <LineStyle><color>ff0000ff</color><tileWidth>3</tileWidth></LineStyle>
                  </Style>
                  <StyleMap id="mapped"><Pair><key>normal</key><styleUrl>#main</styleUrl></Pair></StyleMap>
                  <Placemark><name>Test</name><styleUrl>#mapped</styleUrl>
                    <ExtendedData><SchemaData><SimpleData name="kind">road</SimpleData></SchemaData></ExtendedData>
                    <Point><coordinates>8,47</coordinates></Point>
                  </Placemark>
                  <Placemark/>
                </Document></kml>
                """;
        final var document =
                KmlParser.parse(new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8)));
        final var placemarks = document.placemarks().stream().toList();
        final var first = placemarks.getFirst();
        assertEquals("Test", first.name());
        assertEquals("mapped", first.styleId());
        assertEquals("road", first.extendedData().get("kind"));
        assertNotNull(first.geometry());
        assertNull(first.description());
        final var empty = placemarks.getLast();
        assertNull(empty.name());
        assertNull(empty.geometry());
        assertTrue(empty.extendedData().isEmpty());
        final var style = requireNonNull(document.styles().get("main"));
        assertEquals(2.0, requireNonNull(style.iconStyle()).scale());
        assertEquals("icon.png", requireNonNull(requireNonNull(style.iconStyle()).icon()).href());
        assertEquals("ff0000ff", requireNonNull(style.lineStyle()).color());
        assertEquals(3.0, requireNonNull(style.lineStyle()).width());
        assertNull(style.labelStyle());
        assertEquals(
                "main", requireNonNull(document.styleMaps().get("mapped")).map().get("normal"));
    }
}
