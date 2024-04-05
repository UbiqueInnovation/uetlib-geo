package ch.ubique.backend.geo.tlm;

import static java.util.stream.Collectors.toUnmodifiableMap;

import ch.ubique.backend.core.utils.InputStreamUtils;
import ch.ubique.backend.geo.tile.vector.models.tlm.RailwayInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.UnstructuredTlmInfo;
import ch.ubique.backend.geo.tlm.models.*;
import ch.ubique.backend.geo.utils.GeotoolsUtils;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SwisstopoTlmParser {

    private static final Logger logger = LoggerFactory.getLogger(SwisstopoTlmParser.class);

    private SwisstopoTlmParser() {}

    public static Collection<TlmPath> parsePaths(final InputStream inputStream) throws IOException {
        return InputStreamUtils.inputStreamFileFunction(
                inputStream,
                file -> parseLayer(file, "tlm_strassen_strasse", SwisstopoTlmParser::toTlmPath));
    }

    public static Collection<TlmPath> parsePaths(final File file) throws IOException {
        return parseLayer(file, "tlm_strassen_strasse", SwisstopoTlmParser::toTlmPath);
    }

    public static Collection<TlmFeature<LineString, RailwayInfo>> parseRailways(final File file)
            throws IOException {
        return parseLayer(file, "tlm_oev_eisenbahn", SwisstopoTlmParser::toRailway);
    }

    private static <T> Collection<T> parseLayer(
            final File file,
            final String layer,
            final Function<TlmFeature<Geometry, UnstructuredTlmInfo>, T> mapper)
            throws IOException {
        return parseLayers(file, layer::equals).get(layer).stream().map(mapper).toList();
    }

    public static Map<String, Collection<TlmFeature<Geometry, UnstructuredTlmInfo>>> parseLayers(
            final File file, final Collection<String> layers) throws IOException {
        return parseLayers(file, layers::contains);
    }

    public static Map<String, Collection<TlmFeature<Geometry, UnstructuredTlmInfo>>> parseLayers(
            final File file, final Predicate<String> layerFilter) throws IOException {
        final var results =
                new HashMap<String, Collection<TlmFeature<Geometry, UnstructuredTlmInfo>>>();
        parseLayers(file, layerFilter, results::put);
        return results;
    }

    public static void parseLayers(
            final File file,
            final Predicate<String> layerFilter,
            final BiConsumer<String, Collection<TlmFeature<Geometry, UnstructuredTlmInfo>>>
                    consumer)
            throws IOException {
        logger.info("Parsing swissTLM3D.");

        try (final var geopackage = new GeoPackage(file)) {
            for (final var layer : geopackage.features()) {
                final var layerName = layer.getTableName();
                logger.info("Found layer {}.", layerName);
                final var includeLayer = layerFilter.test(layerName);
                if (!includeLayer) {
                    continue;
                }
                logger.info("Parsing layer {}.", layerName);
                final var reader = geopackage.reader(geopackage.feature(layerName), null, null);
                final var features =
                        GeotoolsUtils.stream(reader).map(SwisstopoTlmParser::parseFeature).toList();
                logger.info("Parsed {} features.", features.size());
                consumer.accept(layerName, features);
                reader.close();
            }
            logger.info("Parsing swissTLM3D finished.");
        }
    }

    private static TlmFeature<Geometry, UnstructuredTlmInfo> parseFeature(
            final SimpleFeature feature) {
        return new TlmFeature<>(
                (Geometry) feature.getDefaultGeometry(),
                new UnstructuredTlmInfo(parseProperties(feature)));
    }

    private static Map<String, Object> parseProperties(final SimpleFeature feature) {
        return feature.getProperties().stream()
                .filter(p -> p.getName() != null && p.getValue() != null)
                .collect(toUnmodifiableMap(p -> p.getName().toString(), Property::getValue));
    }

    private static TlmPath toTlmPath(final TlmFeature<Geometry, UnstructuredTlmInfo> feature) {
        return new TlmPath((LineString) feature.geometry(), feature.info().asPathInfo());
    }

    private static TlmFeature<LineString, RailwayInfo> toRailway(
            final TlmFeature<Geometry, UnstructuredTlmInfo> feature) {
        return new TlmFeature<>((LineString) feature.geometry(), new RailwayInfo(feature.info()));
    }
}
