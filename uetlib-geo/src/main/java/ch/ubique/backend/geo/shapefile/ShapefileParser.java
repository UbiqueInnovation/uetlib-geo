package ch.ubique.backend.geo.shapefile;

import ch.ubique.backend.core.file.ByteFile;
import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputCollectionLoaderService;
import ch.ubique.backend.geo.utils.GeotoolsUtils;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShapefileParser implements InputCollectionLoaderService<SimpleFeature> {

    private static final Logger logger = LoggerFactory.getLogger(ShapefileParser.class);

    private static final Collection<String> relevantFileExtensions =
            Arrays.stream(ShpFileType.values())
                    .map(type -> type.extension)
                    .collect(Collectors.toUnmodifiableSet());

    private final ZipArchiveInputStream zipArchiveInputStream;
    private final Predicate<ArchiveEntry> includeEntry;

    public ShapefileParser(@NotNull final ZipArchiveInputStream zipArchiveInputStream) {
        this(zipArchiveInputStream, t -> true);
    }

    public ShapefileParser(
            @NotNull final ZipArchiveInputStream zipArchiveInputStream,
            @NotNull final Predicate<ArchiveEntry> includeEntry) {
        this.zipArchiveInputStream = Objects.requireNonNull(zipArchiveInputStream);
        this.includeEntry = Objects.requireNonNull(includeEntry);
    }

    @NotNull
    @Override
    public DataAndInput<Collection<SimpleFeature>> loadData() throws IOException {
        final var files = downloadFiles();
        final var features = parseFeatures(files);

        return new DataAndInput<>(features, files);
    }

    private Collection<ByteFile> downloadFiles() throws IOException {
        final var byteFiles = new LinkedList<ByteFile>();

        ArchiveEntry zipEntry;
        while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
            logger.debug("Found zip entry {}", zipEntry);
            if (includeEntry(zipEntry) && includeEntry.test(zipEntry)) {
                byteFiles.add(toByteFile(zipEntry));
                logger.info("Including entry for parsing {}", zipEntry);
            }
        }
        return byteFiles;
    }

    private Collection<SimpleFeature> parseFeatures(final Collection<ByteFile> files)
            throws IOException {

        logger.info("Saving files to temporary directory");
        final var tempDir = Files.createTempDirectory("shapefile-parser");
        final var filePaths = new ArrayList<Path>(files.size());
        for (final var file : files) {
            final var filePath = tempDir.resolve(file.getFileName());
            FileUtils.createParentDirectories(filePath.toFile());
            Files.createFile(filePath);
            try (final var outStream = new FileOutputStream(filePath.toFile())) {
                outStream.write(file.getBytes());
            }
            filePaths.add(filePath);
            logger.info("Saved {} to temporary directory", file.getFileName());
        }

        final var shapeFile =
                filePaths.stream()
                        .filter(f -> f.getFileName().toString().endsWith(".shp"))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No .shp file found."));

        final var dataStore =
                (ShapefileDataStore)
                        DataStoreFinder.getDataStore(
                                Map.of("url", shapeFile.toFile().toURI().toURL()));
        dataStore.setCharset(StandardCharsets.UTF_8);

        final var typeName = dataStore.getTypeNames()[0];

        final var source = dataStore.getFeatureSource(typeName);
        final var filter = Filter.INCLUDE;

        final var featureIterator = source.getFeatures(filter).features();

        final var features = GeotoolsUtils.stream(featureIterator).toList();

        logger.info("Parsed {} features", features.size());

        featureIterator.close();
        dataStore.dispose();

        FileUtils.deleteDirectory(tempDir.toFile());
        logger.info("Deleted temporary directory");

        return features;
    }

    private ByteFile toByteFile(final ArchiveEntry archiveEntry) throws IOException {
        final var bytes = zipArchiveInputStream.readAllBytes();

        return new ByteFile()
                .setFileName(archiveEntry.getName())
                .setBytes(bytes)
                .setContentType("x-gis/x-shapefile");
    }

    private boolean includeEntry(final ArchiveEntry archiveEntry) {
        final var fileName = archiveEntry.getName();
        return !archiveEntry.isDirectory()
                && relevantFileExtensions.stream().anyMatch(fileName::endsWith);
    }
}
