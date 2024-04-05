package ch.ubique.backend.geo.tlm;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.InputStreamUtils;
import ch.ubique.backend.core.utils.ValidationUtils;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.UnstructuredTlmInfo;
import ch.ubique.backend.geo.tlm.models.TlmFeature;
import ch.ubique.backend.geo.tlm.models.TlmPath;

import com.google.common.base.Predicates;

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

        final var paths =
                InputStreamUtils.inputStreamFileFunction(
                        inputStream, SwisstopoTlmParser::parsePaths);

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
        final var paths =
                SwisstopoTlmParser.parseRailways(
                        new File("/Users/daniel/Downloads/tlm_oev_eisenbahn.gpkg"));

        assertFalse(paths.isEmpty());
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

        InputStreamUtils.inputStreamFileConsumer(
                inputStream,
                file ->
                        SwisstopoTlmParser.parseLayers(
                                file, Predicates.alwaysTrue(), this::verifyResults));
    }

    private void verifyResults(
            final String layerName,
            final Collection<TlmFeature<Geometry, UnstructuredTlmInfo>> features) {
        assertNotNull(layerName);
        assertNotNull(features);
        assertFalse(features.isEmpty());
    }
}
