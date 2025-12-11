package ch.ubique.backend.geo.formats.gdb;

import ch.ubique.backend.core.file.temp.TempFileManager;
import ch.ubique.backend.core.io.InputStreamProvider;
import ch.ubique.backend.core.utils.ProcessUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.geotools.geopkg.GeoPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GdbConverter implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TempFileManager tempFileManager;
    private final InputStreamProvider gdbProvider;

    public GdbConverter(final InputStream inputStream) {
        this(InputStreamProvider.of(inputStream));
    }

    public GdbConverter(final InputStreamProvider gdbProvider) {
        this.gdbProvider = gdbProvider;
        this.tempFileManager = new TempFileManager();
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
        ProcessUtils.executeProcess(command).throwIfNotSuccessful();
        logger.debug("Created GeoPackage {}", geoPackagePath);
        return geoPackagePath;
    }

    @Override
    public void close() throws IOException {
        tempFileManager.close();
    }
}
