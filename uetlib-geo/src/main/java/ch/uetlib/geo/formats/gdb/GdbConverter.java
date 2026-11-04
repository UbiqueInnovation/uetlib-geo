package ch.uetlib.geo.formats.gdb;

import static java.util.Objects.requireNonNullElse;

import ch.uetlib.core.file.temp.TempFileManager;
import ch.uetlib.core.io.InputStreamProvider;
import ch.uetlib.core.process.ProcessConfig;
import ch.uetlib.core.utils.ProcessUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.geotools.geopkg.GeoPackage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class GdbConverter implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Duration timeout;
    private final TempFileManager tempFileManager;
    private final InputStreamProvider gdbProvider;

    public GdbConverter(final InputStream inputStream) {
        this(InputStreamProvider.of(inputStream), null);
    }

    public GdbConverter(final InputStreamProvider gdbProvider, @Nullable final Duration timeout) {
        this.gdbProvider = gdbProvider;
        this.tempFileManager = new TempFileManager();
        this.timeout = requireNonNullElse(timeout, Duration.ofMinutes(5));
    }

    public GeoPackage createAndOpenGeoPackage() throws IOException {
        return new GeoPackage(createGeoPackage().toFile());
    }

    public Path createGeoPackage() throws IOException {
        final var gdbPath = extractGdb();
        return convertToGeoPackage(gdbPath);
    }

    private Path extractGdb() throws IOException {
        final var gdbFolder =
                Files.createDirectories(tempFileManager.createTempDir().resolve("input.gdb"));
        logger.debug("Extracting Geo Database (GDB) from {}", gdbProvider);
        try (final var inputStream = gdbProvider.inputStream();
                final var zipArchiveInputStream = new ZipArchiveInputStream(inputStream)) {
            ArchiveEntry zipEntry;
            while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    final var fileName = Path.of(zipEntry.getName()).getFileName().toString();
                    final var filePath = gdbFolder.resolve(fileName);
                    Files.copy(zipArchiveInputStream, filePath);
                }
            }
        }
        return gdbFolder;
    }

    private Path convertToGeoPackage(final Path gdbPath) throws IOException {
        logger.debug("Converting GDB to GeoPackage.");
        final var geoPackagePath = tempFileManager.createTempDir().resolve("output.gpkg");
        final var command = List.of("ogr2ogr", geoPackagePath.toString(), gdbPath.toString());
        ProcessUtils.executeProcess(
                        ProcessConfig.builder().command(command).timeout(timeout).build())
                .throwIfNotSuccessful();
        logger.debug("Created GeoPackage {}", geoPackagePath);
        return geoPackagePath;
    }

    @Override
    public void close() throws IOException {
        tempFileManager.close();
    }
}
