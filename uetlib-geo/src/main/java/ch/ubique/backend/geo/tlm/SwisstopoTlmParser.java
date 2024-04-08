package ch.ubique.backend.geo.tlm;

import static java.util.stream.Collectors.toUnmodifiableMap;

import ch.ubique.backend.core.io.AutoClosableTempFileProvider;
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

    private final GeoPackage geoPackage;

    public SwisstopoTlmParser(final File file) throws IOException {
        this.geoPackage = new GeoPackage(file);
    }

    public static Collection<TlmPath> parsePaths(final InputStream inputStream) throws IOException {
        try (final var fileProvider = new AutoClosableTempFileProvider(inputStream)) {
            return parsePaths(fileProvider.getFile());
        }
    }

    public static Collection<TlmPath> parsePaths(final File file) throws IOException {
        try (final var parser = new SwisstopoTlmParser(file);
                final var reader = parser.getReader("tlm_strassen_strasse")) {
            return parser.parseLayer(reader)
                    .map(f -> new TlmPath((LineString) f.geometry(), f.info().asPathInfo()))
                    .toList();
        }
    }

    public Collection<TlmFeature<LineString, TlmPathInfo>> parsePaths() throws IOException {
        try (final var reader = getReader("tlm_strassen_strasse")) {
            return parseLayer(reader).map(SwisstopoTlmParser::toTlmPath).toList();
        }
    }

    public Collection<TlmFeature<LineString, RailwayInfo>> parseRailways() throws IOException {
        try (final var reader = getReader("tlm_oev_eisenbahn")) {
            return parseLayer(reader).map(SwisstopoTlmParser::toRailway).toList();
        }
    }

    public Collection<TlmFeature<Geometry, UnstructuredTlmInfo>> parseLayer(final String layerName)
            throws IOException {
        logger.info("Parsing layer {}.", layerName);
        try (final var reader = getReader(layerName)) {
            return parseLayer(reader).toList();
        }
    }

    /** Make sure to close the reader! E.g., use try-with-resource */
    public SimpleFeatureReader getReader(final String layerName) throws IOException {
        return geoPackage.reader(geoPackage.feature(layerName), null, null);
    }

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
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
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
    public void close() {
        geoPackage.close();
    }
}
