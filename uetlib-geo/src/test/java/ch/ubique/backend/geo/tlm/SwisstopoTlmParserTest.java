package ch.ubique.backend.geo.tlm;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.ValidationUtils;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.UnstructuredTlmInfo;
import ch.ubique.backend.geo.tlm.models.TlmFeature;
import ch.ubique.backend.geo.tlm.models.TlmPath;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

class SwisstopoTlmParserTest {

    @Test
    @Disabled("Long running")
    void testSwisstopoTlmParser() throws IOException {
        final var inputStream =
                new ZipArchiveInputStream(
                        URI.create(
                                        "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.gpkg.zip")
                                .toURL()
                                .openStream());
        inputStream.getNextEntry();

        final var paths = SwisstopoTlmParser.parsePaths(inputStream);

        assertFalse(paths.isEmpty());
        for (final var path : paths) {
            assertEquals(
                    Set.of(), ValidationUtils.validate(path), "Expected no constraint violations");
        }

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::trailType)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::level)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::surfaceType)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::significanceForTraffic)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::name)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::streetName)
                        .anyMatch(Objects::nonNull));

        assertTrue(
                paths.stream()
                        .map(TlmPath::info)
                        .map(TlmPathInfo::passable)
                        .anyMatch(Objects::nonNull));
    }

    @Test
    @Disabled("Depends on local file")
    void testRailways() throws IOException {
        final var file = new File("/Users/daniel/Downloads/tlm_oev_eisenbahn.gpkg");
        try (final var parser = new SwisstopoTlmParser(file)) {
            final var paths = parser.parseRailways();
            assertFalse(paths.isEmpty());
        }
    }

    @Test
    @Disabled("Long running")
    void testParseAll() throws IOException {

        final var inputStream =
                new ZipArchiveInputStream(
                        URI.create(
                                        "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.gpkg.zip")
                                .toURL()
                                .openStream());

        inputStream.getNextEntry();
        try (final var parser = new SwisstopoTlmParser(inputStream)) {
            for (final var layer : parser.getLayers()) {
                final var features = parser.parseLayer(layer);
                verifyResults(layer, features);
            }
        }
    }

    private void verifyResults(
            final String layerName,
            final Collection<TlmFeature<Geometry, UnstructuredTlmInfo>> features) {
        assertNotNull(layerName);
        assertNotNull(features);
        assertTrue(CollectionUtils.isNotEmpty(features));
    }
}
