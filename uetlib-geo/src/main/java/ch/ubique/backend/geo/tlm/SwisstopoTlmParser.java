package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.core.io.FileProvider;
import ch.ubique.backend.core.utils.MapCollectors;
import ch.ubique.backend.geo.tile.vector.models.tlm.RailwayInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.UnstructuredTlmInfo;
import ch.ubique.backend.geo.tlm.models.*;
import ch.ubique.backend.geo.utils.GeotoolsUtils;

import org.geotools.api.data.SimpleFeatureReader;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.geopkg.Entry;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class SwisstopoTlmParser implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FileProvider fileProvider;
    private final GeoPackage geoPackage;

    public SwisstopoTlmParser(final File file) throws IOException {
        this(FileProvider.of(file));
    }

    public SwisstopoTlmParser(final InputStream inputStream) throws IOException {
        this(FileProvider.of(inputStream));
    }

    public SwisstopoTlmParser(final FileProvider fileProvider) throws IOException {
        this.fileProvider = fileProvider;
        this.geoPackage = new GeoPackage(fileProvider.file());
    }

    public static Collection<TlmPath> parsePaths(final InputStream inputStream) throws IOException {
        return parsePaths(FileProvider.of(inputStream));
    }

    public static Collection<TlmPath> parsePaths(final File file) throws IOException {
        return parsePaths(FileProvider.of(file));
    }

    public static Collection<TlmPath> parsePaths(final FileProvider fileProvider)
            throws IOException {
        try (final var parser = new SwisstopoTlmParser(fileProvider)) {
            final var reader = parser.getReader("tlm_strassen_strasse");
            return GeotoolsUtils.withReader(
                    reader,
                    stream ->
                            stream.map(parser::parseFeature)
                                    .map(
                                            f ->
                                                    new TlmPath(
                                                            (LineString) f.geometry(),
                                                            f.info().asPathInfo()))
                                    .toList());
        }
    }

    public Collection<TlmFeature<LineString, TlmPathInfo>> parsePaths() throws IOException {
        final var reader = getReader("tlm_strassen_strasse");
        return GeotoolsUtils.withReader(
                reader,
                stream ->
                        stream.map(this::parseFeature).map(SwisstopoTlmParser::toTlmPath).toList());
    }

    public Collection<TlmFeature<LineString, RailwayInfo>> parseRailways() throws IOException {
        final var reader = getReader("tlm_oev_eisenbahn");
        return GeotoolsUtils.withReader(
                reader,
                stream ->
                        stream.map(this::parseFeature).map(SwisstopoTlmParser::toRailway).toList());
    }

    public Collection<TlmFeature<Geometry, UnstructuredTlmInfo>> parseLayer(final String layerName)
            throws IOException {
        logger.info("Parsing layer {}.", layerName);
        final var reader = getReader(layerName);
        return GeotoolsUtils.withReader(reader, stream -> stream.map(this::parseFeature).toList());
    }

    /**
     * Prefer {@link GeotoolsUtils#withReader} or close the returned stream from parseLayer(reader).
     */
    public SimpleFeatureReader getReader(final String layerName) throws IOException {
        return geoPackage.reader(geoPackage.feature(layerName), null, null);
    }

    /** Caller must close the returned stream, e.g. via try-with-resources. */
    public Stream<TlmFeature<Geometry, UnstructuredTlmInfo>> parseLayer(
            final SimpleFeatureReader reader) {
        return GeotoolsUtils.stream(reader).map(this::parseFeature);
    }

    public Collection<String> getLayers() throws IOException {
        return geoPackage.features().stream().map(Entry::getTableName).toList();
    }

    private TlmFeature<Geometry, UnstructuredTlmInfo> parseFeature(final SimpleFeature feature) {
        return new TlmFeature<>(
                (Geometry) feature.getDefaultGeometry(),
                new UnstructuredTlmInfo(parseProperties(feature)));
    }

    private static Map<String, Object> parseProperties(final SimpleFeature feature) {
        return Stream.concat(
                        Stream.of(Map.entry("id", feature.getID())),
                        feature.getProperties().stream()
                                .filter(p -> p.getName() != null && p.getValue() != null)
                                .map(p -> Map.entry(p.getName().toString(), p.getValue())))
                .collect(MapCollectors.entriesToMapDropDuplicates());
    }

    public static TlmFeature<LineString, TlmPathInfo> toTlmPath(
            final TlmFeature<Geometry, UnstructuredTlmInfo> feature) {
        return new TlmFeature<>((LineString) feature.geometry(), feature.info().asPathInfo());
    }

    public static TlmFeature<LineString, RailwayInfo> toRailway(
            final TlmFeature<Geometry, UnstructuredTlmInfo> feature) {
        return new TlmFeature<>((LineString) feature.geometry(), new RailwayInfo(feature.info()));
    }

    @Override
    public void close() throws IOException {
        geoPackage.close();
        fileProvider.close();
    }
}
