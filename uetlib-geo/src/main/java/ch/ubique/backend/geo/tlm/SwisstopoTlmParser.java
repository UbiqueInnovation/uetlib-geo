package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tile.vector.models.tlm.TrafficRestriction;
import ch.ubique.backend.geo.tlm.models.*;
import ch.ubique.backend.geo.utils.GeotoolsUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geopkg.GeoPackage;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SwisstopoTlmParser {

    private static final Logger logger = LoggerFactory.getLogger(SwisstopoTlmParser.class);

    private SwisstopoTlmParser() {}

    public static Collection<TlmPath> parsePaths(final InputStream inputStream) throws IOException {

        final var path = Files.createTempFile("swissTLM3D-", ".gpkg");
        final var file = path.toFile();

        logger.info("Saving input to temporary file {}.", path);
        FileUtils.copyToFile(inputStream, file);

        final var paths = parsePaths(file);

        logger.info("Deleted temporary file.");
        Files.delete(path);

        return paths;
    }

    public static Collection<TlmPath> parsePaths(final File file) throws IOException {
        logger.info("Parsing swissTLM3D.");

        try (final var geopackage = new GeoPackage(file)) {

            final var streetsFeatureReader =
                    geopackage.reader(geopackage.feature("tlm_strassen_strasse"), null, null);

            final var paths =
                    GeotoolsUtils.stream(streetsFeatureReader)
                            .map(SwisstopoTlmParser::toTlmPath)
                            .toList();

            logger.info("Parsing swissTLM3D finished.");
            return paths;
        }
    }

    private static TlmPath toTlmPath(final SimpleFeature feature) {
        return new TlmPath(
                (LineString) feature.getDefaultGeometry(),
                new TlmPathInfo(
                        asString(feature, "uuid").orElseThrow(),
                        asString(feature, "objektart").map(WegType::parse).orElseThrow(),
                        asEnum(feature, WanderwegType.class, "wanderwege").orElse(null),
                        asString(feature, "stufe").map(SwisstopoTlmParser::parseStufe).orElse(null),
                        asEnum(feature, BelagType.class, "belagsart").orElse(null),
                        asEnum(feature, Verkehrsbedeutung.class, "verkehrsbedeutung").orElse(null),
                        asString(feature, "verkehrsbeschraenkung")
                                .map(TrafficRestriction::parse)
                                .orElse(TrafficRestriction.NONE),
                        asString(feature, "name").orElse(null),
                        asString(feature, "strassenname").orElse(null),
                        asBoolean(feature, "richtungsgetrennt").orElse(false),
                        asBoolean(feature, "kreisel").orElse(false),
                        asBoolean(feature, "befahrbarkeit").orElse(null)));
    }

    private static Optional<Boolean> asBoolean(final SimpleFeature feature, final String key) {
        return asString(feature, key).map(SwisstopoTlmParser::parseBoolean);
    }

    private static Boolean parseBoolean(final String value) {
        return switch (value.toLowerCase()) {
            case "wahr" -> true;
            case "falsch" -> false;
            default -> throw new IllegalStateException(
                    "Unexpected boolean value: " + value.toLowerCase());
        };
    }

    private static <E extends Enum<E>> Optional<E> asEnum(
            final SimpleFeature feature, final Class<E> enumClass, final String key) {
        return asString(feature, key)
                .map(value -> Enum.valueOf(enumClass, value.toUpperCase().replace(' ', '_')));
    }

    private static int parseStufe(final String value) {
        final var split = value.split("_");
        final var stufe = Integer.parseInt(split[split.length - 1]);
        if (value.contains("minus")) {
            return -stufe;
        }
        return stufe;
    }

    private static final Collection<String> NULL_VALUES = List.of("k_W", "ub");

    private static Optional<String> asString(final SimpleFeature feature, final String key) {
        final var value = (String) feature.getAttribute(key);
        if (StringUtils.isBlank(value) || NULL_VALUES.contains(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
