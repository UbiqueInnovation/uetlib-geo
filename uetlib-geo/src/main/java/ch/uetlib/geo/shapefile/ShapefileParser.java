package ch.uetlib.geo.shapefile;

import static java.util.Objects.requireNonNull;

import ch.uetlib.core.file.ByteFile;
import ch.uetlib.core.file.temp.TempFileManager;
import ch.uetlib.core.service.DataAndInput;
import ch.uetlib.core.service.InputCollectionLoaderService;
import ch.uetlib.geo.utils.GeotoolsUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFileType;
import org.jspecify.annotations.NonNull;
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShapefileParser implements InputCollectionLoaderService<SimpleFeature> {

    private static final Logger logger = LoggerFactory.getLogger(ShapefileParser.class);

    private static final Collection<String> relevantFileExtensions =
            Arrays.stream(ShpFileType.values())
                    .map(type -> type.extension)
                    .collect(Collectors.toUnmodifiableSet());

    private final ZipArchiveInputStream zipArchiveInputStream;
    private final Predicate<ArchiveEntry> includeEntry;

    public ShapefileParser(@NonNull final ZipArchiveInputStream zipArchiveInputStream) {
        this(zipArchiveInputStream, t -> true);
    }

    public ShapefileParser(
            @NonNull final ZipArchiveInputStream zipArchiveInputStream,
            @NonNull final Predicate<ArchiveEntry> includeEntry) {
        this.zipArchiveInputStream = requireNonNull(zipArchiveInputStream);
        this.includeEntry = requireNonNull(includeEntry);
    }

    @NonNull
    @Override
    public DataAndInput<Collection<SimpleFeature>> loadData() throws IOException {
        final var files = downloadFiles();
        final var features = parseFeaturesFromByteFiles(files);

        return new DataAndInput<>(features, files);
    }

    @NonNull
    public Collection<SimpleFeature> parseFeatures() throws IOException {
        try (final var tmpFileManager = new TempFileManager()) {
            final var directory = tmpFileManager.createTempDir();
            final var files = copyNeededFilesToDirectory(directory);
            return parseFeaturesFromFiles(files);
        }
    }

    private Collection<Path> copyNeededFilesToDirectory(final Path directory) throws IOException {
        final var filePaths = new ArrayList<Path>();
        ArchiveEntry zipEntry;
        while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
            if (isEntryNeeded(zipEntry)) {
                final var fileName = Path.of(zipEntry.getName()).getFileName().toString();
                final var filePath = directory.resolve(fileName);
                Files.copy(zipArchiveInputStream, filePath);
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    private boolean isEntryNeeded(final ArchiveEntry zipEntry) {
        final var name = zipEntry.getName();
        if (!isRelevant(zipEntry)) {
            logger.trace("{} is not relevant.", name);
            return false;
        }
        if (includeEntry.test(zipEntry)) {
            logger.debug("Including {} for parsing.", name);
            return true;
        }
        logger.trace("Ignoring {} for parsing.", name);
        return false;
    }

    private Collection<ByteFile> downloadFiles() throws IOException {
        final var byteFiles = new ArrayList<ByteFile>();

        ArchiveEntry zipEntry;
        while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
            if (isEntryNeeded(zipEntry)) {
                byteFiles.add(toByteFile(zipEntry));
            }
        }
        return byteFiles;
    }

    @SuppressWarnings("java:S5443")
    private Collection<SimpleFeature> parseFeaturesFromByteFiles(final Collection<ByteFile> files)
            throws IOException {

        logger.debug("Saving files to temporary directory");
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
            logger.debug("Saved {} to temporary directory", file.getFileName());
        }

        final var features = parseFeaturesFromFiles(filePaths);

        FileUtils.deleteDirectory(tempDir.toFile());
        logger.debug("Deleted temporary directory");

        return features;
    }

    private static Collection<SimpleFeature> parseFeaturesFromFiles(
            final Collection<Path> filePaths) throws IOException {

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
        try {
            final var typeName = dataStore.getTypeNames()[0];
            final var source = dataStore.getFeatureSource(typeName);
            final var filter = Filter.INCLUDE;
            final var featureIterator = source.getFeatures(filter).features();
            final var features = GeotoolsUtils.withIterator(featureIterator, Stream::toList);
            logger.debug("Parsed {} features", features.size());
            return features;
        } finally {
            dataStore.dispose();
        }
    }

    private ByteFile toByteFile(final ArchiveEntry archiveEntry) throws IOException {
        final var bytes = zipArchiveInputStream.readAllBytes();

        return new ByteFile()
                .setFileName(archiveEntry.getName())
                .setBytes(bytes)
                .setContentType("x-gis/x-shapefile");
    }

    private boolean isRelevant(final ArchiveEntry archiveEntry) {
        final var fileName = archiveEntry.getName();
        return !archiveEntry.isDirectory()
                && relevantFileExtensions.stream().anyMatch(fileName::endsWith);
    }
}
