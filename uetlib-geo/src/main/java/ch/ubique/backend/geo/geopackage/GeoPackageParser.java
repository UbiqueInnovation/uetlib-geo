package ch.ubique.backend.geo.geopackage;

import com.google.common.collect.MoreCollectors;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.user.UserCoreRow;
import mil.nga.sf.wkt.GeometryWriter;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @deprecated Please use Geotools for parsing geo packages
 */
@Deprecated(since = "2.3.0", forRemoval = true)
public class GeoPackageParser implements AutoCloseable {

    private final GeoPackage geoPackage;
    private final GeometryFactory geometryFactory;
    private final File geoPackageTemporaryFile;

    @SuppressWarnings("java:S5443")
    public GeoPackageParser(final InputStream inputStream, final GeometryFactory geometryFactory)
            throws IOException {
        this.geometryFactory = geometryFactory;
        this.geoPackageTemporaryFile = Files.createTempFile(null, ".gpkg").toFile();
        FileUtils.copyInputStreamToFile(inputStream, geoPackageTemporaryFile);
        this.geoPackage = GeoPackageManager.open(geoPackageTemporaryFile);
    }

    @Override
    public void close() throws IOException {
        Files.delete(geoPackageTemporaryFile.toPath());
        geoPackage.close();
    }

    public GeoPackageData parse() {
        final var featureTableName =
                geoPackage.getFeatureTables().stream().collect(MoreCollectors.onlyElement());

        final var attributes =
                geoPackage.getAttributesTables().stream()
                        .collect(Collectors.toMap(Function.identity(), this::getAttributesMap));

        final var featureQuery = geoPackage.getFeatureDao(featureTableName).queryForAll();
        final var features =
                StreamSupport.stream(featureQuery.spliterator(), false)
                        .map(row -> new GeoPackageFeature(getJtsGeometry(row), getAttributes(row)))
                        .toList();

        return new GeoPackageData(features, attributes);
    }

    private Collection<Map<String, Object>> getAttributesMap(final String attributesTableName) {
        return StreamSupport.stream(
                        geoPackage
                                .getAttributesDao(attributesTableName)
                                .queryForAll()
                                .spliterator(),
                        false)
                .map(GeoPackageParser::getAttributes)
                .toList();
    }

    private static Map<String, Object> getAttributes(final UserCoreRow<?, ?> row) {
        return Arrays.stream(row.getColumnNames())
                .filter(key -> key != null && row.getValue(key) != null)
                .collect(Collectors.toMap(Function.identity(), row::getValue));
    }

    @SuppressWarnings("java:S112")
    private Geometry getJtsGeometry(final FeatureRow row) {
        try {
            final var wkt = GeometryWriter.writeGeometry(row.getGeometry().getGeometry());
            return new WKTReader(geometryFactory).read(wkt);
        } catch (final ParseException | IOException e) {
            throw new RuntimeException("Could not convert geometry to JTS.", e);
        }
    }
}
