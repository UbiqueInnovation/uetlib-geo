package ch.ubique.backend.geo.tlm;

import static org.junit.jupiter.api.Assertions.*;

import ch.ubique.backend.core.utils.ValidationUtils;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tlm.models.TlmPath;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

class SwisstopoTlmParserTest {

    @Test
    @Disabled("Long running")
    void testSwisstopoTlmParser() throws IOException {
        final var inputStream =
                new ZipArchiveInputStream(
                        new URL(
                                        "https://data.geo.admin.ch/ch.swisstopo.swisstlm3d/swisstlm3d_2023-03/swisstlm3d_2023-03_2056_5728.gpkg.zip")
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
}
